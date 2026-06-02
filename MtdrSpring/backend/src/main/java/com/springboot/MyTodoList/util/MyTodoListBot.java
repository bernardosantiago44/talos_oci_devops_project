package com.springboot.MyTodoList.util;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItemPriority;
import com.springboot.MyTodoList.repository.AppUserRepository;
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
    private final DeepSeekService deepSeekService;
    private final SprintService sprintService;

    private final Map<Long, String> userState = new ConcurrentHashMap<>();

    public MyTodoListBot(
            @Value("${telegram.bot.token:}") String telegramBotToken,
            WorkItemService workItemService,
            AppUserRepository appUserRepository,
            DeepSeekService deepSeekService,
            SprintService sprintService) {
        this.telegramBotToken = telegramBotToken;
        this.telegramClient = new OkHttpTelegramClient(telegramBotToken);
        this.workItemService = workItemService;
        this.appUserRepository = appUserRepository;
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

        if (text.equals(BotCommands.START_COMMAND.getCommand())) {
            handleStart(chatId);
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

    private void handleStart(long chatId) {
        ReplyKeyboardMarkup keyboard = buildMainKeyboard();
        BotHelper.sendMessageToTelegram(chatId, BotMessages.HELLO_MYTODO_BOT.getMessage(), telegramClient, keyboard);
        userState.remove(chatId);
    }

    private void handleHide(long chatId) {
        BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), telegramClient);
        userState.remove(chatId);
    }

    private void handleTodoList(long chatId, String telegramId) {
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
            BotHelper.sendMessageToTelegram(chatId,
                    "Your Telegram account is not linked to any user in the system. Contact your administrator.",
                    telegramClient);
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

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}
