package com.springboot.MyTodoList.util;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.model.WorkItemPriority;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.service.AppUserService;
import com.springboot.MyTodoList.service.DeepSeekService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.WorkItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnExpression("!'${telegram.bot.token:}'.trim().isEmpty()")
public class MyTodoListBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MyTodoListBot.class);

    private final TelegramClient telegramClient;
    private final String telegramBotToken;
    private final WorkItemService workItemService;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final DeepSeekService deepSeekService;
    private final SprintService sprintService;

    private final Map<Long, String> userState = new ConcurrentHashMap<>();

    public MyTodoListBot(
            @Value("${telegram.bot.token:}") String telegramBotToken,
            WorkItemService workItemService,
            AppUserRepository appUserRepository,
            AppUserService appUserService,
            DeepSeekService deepSeekService,
            SprintService sprintService) {
        this.telegramBotToken = telegramBotToken;
        this.telegramClient = new OkHttpTelegramClient(telegramBotToken);
        this.workItemService = workItemService;
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
        this.deepSeekService = deepSeekService;
        this.sprintService = sprintService;
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        String telegramId = String.valueOf(update.getMessage().getFrom().getId());
        String state = userState.get(chatId);

        try {
            dispatch(chatId, telegramId, text, state);
        } catch (Exception e) {
            logger.error("Error handling message chatId={} text={}", chatId, text, e);
            BotHelper.sendMessageToTelegram(chatId, "Something went wrong: " + e.getMessage(), telegramClient);
        }
    }

    private void dispatch(long chatId, String telegramId, String text, String state) {

        // If user is not linked yet, intercept everything except the linking flow
        if ("WAITING_USER_LINK".equals(state)) {
            handleUserLink(chatId, telegramId, text);
            return;
        }

        Optional<AppUser> linkedUser = appUserRepository.findByTelegramUserId(telegramId);
        if (linkedUser.isEmpty() && !text.equals(BotCommands.START_COMMAND.getCommand())) {
            askUserToIdentify(chatId);
            return;
        }

        if (text.equals(BotCommands.START_COMMAND.getCommand())) {
            if (linkedUser.isEmpty()) {
                askUserToIdentify(chatId);
            } else {
                handleStart(chatId, linkedUser.get().getName());
            }
        } else if (text.equals(BotCommands.HIDE_COMMAND.getCommand())) {
            handleHide(chatId);
        } else if (text.equals(BotCommands.TODO_LIST.getCommand())) {
            handleTodoList(chatId, telegramId);
        } else if (text.equals(BotCommands.ADD_ITEM.getCommand())) {
            handleAddItemStart(chatId);
        } else if (text.startsWith(BotCommands.LLM_REQ.getCommand())) {
            String prompt = text.substring(BotCommands.LLM_REQ.getCommand().length()).trim();
            handleLlm(chatId, prompt);
        } else if ("WAITING_ITEM_TITLE".equals(state)) {
            handleAddItemTitle(chatId, telegramId, text);
        } else if ("WAITING_LLM".equals(state)) {
            handleLlmPrompt(chatId, text);
        } else {
            BotHelper.sendMessageToTelegram(chatId, "Unknown command. Use /start to see options.", telegramClient);
        }
    }

    private void askUserToIdentify(long chatId) {
        List<AppUserSummary> users = appUserService.findAll();
        if (users.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No users found in the system. Contact your administrator.", telegramClient);
            return;
        }

        ReplyKeyboardMarkup keyboard = buildUserSelectionKeyboard(users);
        userState.put(chatId, "WAITING_USER_LINK");
        BotHelper.sendMessageToTelegram(chatId,
                "Who are you? Select your name to link your Telegram account:",
                telegramClient, keyboard);
    }

    private void handleUserLink(long chatId, String telegramId, String selectedName) {
        List<AppUserSummary> users = appUserService.findAll();
        Optional<AppUserSummary> match = users.stream()
                .filter(u -> u.name().equalsIgnoreCase(selectedName))
                .findFirst();

        if (match.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId,
                    "Name not recognized. Please select your name from the list.",
                    telegramClient, buildUserSelectionKeyboard(users));
            return;
        }

        AppUser user = appUserRepository.findById(match.get().userId()).orElse(null);
        if (user == null) {
            BotHelper.sendMessageToTelegram(chatId, "Error finding your account. Try again later.", telegramClient);
            return;
        }

        user.setTelegramUserId(telegramId);
        appUserRepository.save(user);
        userState.remove(chatId);

        handleStart(chatId, user.getName());
    }

    private void handleStart(long chatId, String userName) {
        String greeting = "Welcome, " + userName + "! " + BotMessages.HELLO_MYTODO_BOT.getMessage();
        BotHelper.sendMessageToTelegram(chatId, greeting, telegramClient, buildMainKeyboard());
        userState.remove(chatId);
    }

    private void handleHide(long chatId) {
        BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), telegramClient);
        userState.remove(chatId);
    }

    private void handleTodoList(long chatId, String telegramId) {
        Optional<AppUser> user = appUserRepository.findByTelegramUserId(telegramId);
        if (user.isEmpty()) {
            askUserToIdentify(chatId);
            return;
        }

        List<WorkItemResponse> items = workItemService.findByTelegramUserId(telegramId);
        if (items.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "You have no work items assigned.", telegramClient);
            return;
        }
        StringBuilder sb = new StringBuilder("Your work items:\n\n");
        for (WorkItemResponse item : items) {
            sb.append("• [").append(item.status()).append("] ").append(item.title());
            if (item.estimatedMinutes() != null) {
                sb.append(" (~").append(item.estimatedMinutes()).append(" min)");
            }
            sb.append("\n");
        }
        BotHelper.sendMessageToTelegram(chatId, sb.toString(), telegramClient);
    }

    private void handleAddItemStart(long chatId) {
        userState.put(chatId, "WAITING_ITEM_TITLE");
        BotHelper.sendMessageToTelegram(chatId, BotMessages.TYPE_NEW_TODO_ITEM.getMessage(), telegramClient);
    }

    private void handleAddItemTitle(long chatId, String telegramId, String title) {
        userState.remove(chatId);

        Optional<AppUser> user = appUserRepository.findByTelegramUserId(telegramId);
        if (user.isEmpty()) {
            askUserToIdentify(chatId);
            return;
        }

        List<SprintResponse> sprints = sprintService.findAll();
        Optional<SprintResponse> activeSprint = sprints.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.status()))
                .findFirst();

        if (activeSprint.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No active sprint found. Cannot create work item.", telegramClient);
            return;
        }

        CreateWorkItemRequest req = new CreateWorkItemRequest();
        req.setSprintId(activeSprint.get().sprintId());
        req.setCreatedByUserId(user.get().getUserId());
        req.setTitle(title);
        req.setWorkType("TASK");
        req.setStatus("NEW");
        req.setPriority(WorkItemPriority.MEDIUM);
        req.setAssigneeIds(List.of(user.get().getUserId()));

        workItemService.createWorkItem(req);
        BotHelper.sendMessageToTelegram(chatId, BotMessages.NEW_ITEM_ADDED.getMessage(), telegramClient);
    }

    private void handleLlm(long chatId, String prompt) {
        if (prompt.isBlank()) {
            userState.put(chatId, "WAITING_LLM");
            BotHelper.sendMessageToTelegram(chatId, "What would you like to ask?", telegramClient);
        } else {
            handleLlmPrompt(chatId, prompt);
        }
    }

    private void handleLlmPrompt(long chatId, String prompt) {
        userState.remove(chatId);
        BotHelper.sendMessageToTelegram(chatId, "Thinking...", telegramClient);
        try {
            String response = deepSeekService.generateText(prompt);
            BotHelper.sendMessageToTelegram(chatId, response, telegramClient);
        } catch (Exception e) {
            logger.error("LLM request failed for chatId={}", chatId, e);
            BotHelper.sendMessageToTelegram(chatId, "Sorry, I couldn't process your request. Please try again.", telegramClient);
        }
    }

    private ReplyKeyboardMarkup buildMainKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(BotCommands.TODO_LIST.getCommand());
        row1.add(BotCommands.ADD_ITEM.getCommand());
        KeyboardRow row2 = new KeyboardRow();
        row2.add(BotCommands.LLM_REQ.getCommand());
        row2.add(BotCommands.HIDE_COMMAND.getCommand());
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup buildUserSelectionKeyboard(List<AppUserSummary> users) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < users.size(); i++) {
            row.add(users.get(i).name());
            // Max 2 names per row
            if (row.size() == 2 || i == users.size() - 1) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}
