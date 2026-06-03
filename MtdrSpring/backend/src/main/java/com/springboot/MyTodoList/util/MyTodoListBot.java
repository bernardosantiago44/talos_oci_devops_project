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

    private static final String SKIP = "/skip";

    private final TelegramClient telegramClient;
    private final String telegramBotToken;
    private final WorkItemService workItemService;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final DeepSeekService deepSeekService;
    private final SprintService sprintService;

    private final Map<Long, String> userState = new ConcurrentHashMap<>();
    // Stores the in-progress work item form per chat
    private final Map<Long, CreateWorkItemRequest> pendingItems = new ConcurrentHashMap<>();

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
        if ("WAITING_USER_LINK".equals(state)) {
            handleUserLink(chatId, telegramId, text);
            return;
        }

        // Multi-step add item form
        if (state != null && state.startsWith("ITEM_")) {
            handleAddItemStep(chatId, telegramId, text, state);
            return;
        }

        // Todolist sub-menu states
        if ("TODOLIST_COMPLETED_SPRINT".equals(state)) {
            handleTodoListCompletedSprint(chatId, telegramId, text);
            return;
        }
        if ("TODOLIST_BY_SPRINT".equals(state)) {
            handleTodoListBySprint(chatId, telegramId, text);
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
        } else if (text.equals(TODO_ACTIVE) || text.equals(TODO_COMPLETED)
                || text.equals(TODO_BY_SPRINT) || text.equals(TODO_ALL)) {
            handleTodoListMenu(chatId, telegramId, text);
        } else if (text.equals(BotCommands.ADD_ITEM.getCommand())) {
            handleAddItemStart(chatId, telegramId);
        } else if (text.startsWith(BotCommands.LLM_REQ.getCommand())) {
            String prompt = text.substring(BotCommands.LLM_REQ.getCommand().length()).trim();
            handleLlm(chatId, prompt);
        } else if ("WAITING_LLM".equals(state)) {
            handleLlmPrompt(chatId, text);
        } else {
            BotHelper.sendMessageToTelegram(chatId, "Unknown command. Use /start to see options.", telegramClient);
        }
    }

    // --- User linking ---

    private void askUserToIdentify(long chatId) {
        List<AppUserSummary> users = appUserService.findAll();
        if (users.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No users found in the system. Contact your administrator.", telegramClient);
            return;
        }
        userState.put(chatId, "WAITING_USER_LINK");
        BotHelper.sendMessageToTelegram(chatId,
                "Who are you? Select your name to link your Telegram account:",
                telegramClient, buildUserSelectionKeyboard(users));
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

    // --- Main screens ---

    private void handleStart(long chatId, String userName) {
        String greeting = "Welcome, " + userName + "! " + BotMessages.HELLO_MYTODO_BOT.getMessage();
        BotHelper.sendMessageToTelegram(chatId, greeting, telegramClient, buildMainKeyboard());
        userState.remove(chatId);
        pendingItems.remove(chatId);
    }

    private void handleHide(long chatId) {
        BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), telegramClient);
        userState.remove(chatId);
        pendingItems.remove(chatId);
    }

    // --- Todo list ---

    private static final String TODO_ACTIVE    = "Active Tasks";
    private static final String TODO_COMPLETED = "Completed Tasks";
    private static final String TODO_BY_SPRINT = "By Sprint";
    private static final String TODO_ALL       = "All Tasks";

    private static final List<String> ACTIVE_STATUSES = List.of("NEW", "TODO", "IN_PROGRESS", "BLOCKED");

    private void handleTodoList(long chatId, String telegramId) {
        Optional<AppUser> user = appUserRepository.findByTelegramUserId(telegramId);
        if (user.isEmpty()) {
            askUserToIdentify(chatId);
            return;
        }

        // Show sub-menu
        KeyboardRow row1 = new KeyboardRow();
        row1.add(TODO_ACTIVE);
        row1.add(TODO_COMPLETED);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(TODO_BY_SPRINT);
        row2.add(TODO_ALL);
        ReplyKeyboardMarkup menu = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
        BotHelper.sendMessageToTelegram(chatId, "What tasks do you want to see?", telegramClient, menu);
    }

    private void handleTodoListMenu(long chatId, String telegramId, String option) {
        switch (option) {
            case TODO_ACTIVE -> {
                List<WorkItemResponse> items = workItemService.findByTelegramUserId(telegramId).stream()
                        .filter(i -> ACTIVE_STATUSES.contains(i.status()))
                        .toList();
                sendTaskList(chatId, items, "Active tasks");
            }
            case TODO_ALL -> {
                List<WorkItemResponse> items = workItemService.findByTelegramUserId(telegramId);
                sendTaskList(chatId, items, "All tasks");
            }
            case TODO_COMPLETED -> {
                userState.put(chatId, "TODOLIST_COMPLETED_SPRINT");
                askSprintSelection(chatId, "completed tasks");
            }
            case TODO_BY_SPRINT -> {
                userState.put(chatId, "TODOLIST_BY_SPRINT");
                askSprintSelection(chatId, "tasks");
            }
            default -> handleTodoList(chatId, telegramId);
        }
    }

    private void handleTodoListCompletedSprint(long chatId, String telegramId, String text) {
        userState.remove(chatId);
        String sprintId = text.contains(" — ") ? text.split(" — ")[0].trim() : null;
        List<WorkItemResponse> items = workItemService.findByTelegramUserId(telegramId).stream()
                .filter(i -> "DONE".equals(i.status()))
                .filter(i -> sprintId == null || sprintId.equals(i.sprintId()))
                .toList();
        String label = sprintId != null ? "Completed in " + text : "All completed tasks";
        sendTaskList(chatId, items, label);
    }

    private void handleTodoListBySprint(long chatId, String telegramId, String text) {
        userState.remove(chatId);
        String sprintId = text.contains(" — ") ? text.split(" — ")[0].trim() : null;
        List<WorkItemResponse> items = workItemService.findByTelegramUserId(telegramId).stream()
                .filter(i -> sprintId == null || sprintId.equals(i.sprintId()))
                .toList();
        String label = sprintId != null ? "Tasks in " + text : "All tasks";
        sendTaskList(chatId, items, label);
    }

    private void askSprintSelection(long chatId, String context) {
        List<SprintResponse> sprints = sprintService.findAll();
        List<String> options = new ArrayList<>();
        sprints.forEach(s -> options.add(s.sprintId() + " — " + s.name()));
        options.add("All Sprints");
        BotHelper.sendMessageToTelegram(chatId,
                "Which sprint's " + context + " do you want to see?",
                telegramClient, buildOptionsKeyboard(options, true));
    }

    private void sendTaskList(long chatId, List<WorkItemResponse> items, String label) {
        if (items.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No tasks found for: " + label, telegramClient);
            return;
        }
        StringBuilder sb = new StringBuilder(label + ":\n\n");
        for (WorkItemResponse item : items) {
            sb.append("• [").append(item.status()).append("] ").append(item.title());
            if (item.estimatedMinutes() != null) {
                sb.append(" (~").append(item.estimatedMinutes()).append(" min)");
            }
            sb.append("\n");
        }
        BotHelper.sendMessageToTelegram(chatId, sb.toString(), telegramClient);
    }

    // --- Add item multi-step form ---

    private void handleAddItemStart(long chatId, String telegramId) {
        Optional<AppUser> user = appUserRepository.findByTelegramUserId(telegramId);
        if (user.isEmpty()) {
            askUserToIdentify(chatId);
            return;
        }

        List<SprintResponse> sprints = sprintService.findAll();
        if (sprints.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No sprints found. Cannot create work item.", telegramClient);
            return;
        }

        CreateWorkItemRequest req = new CreateWorkItemRequest();
        req.setCreatedByUserId(user.get().getUserId());
        req.setStatus("NEW");
        req.setAssigneeIds(List.of(user.get().getUserId()));

        pendingItems.put(chatId, req);
        userState.put(chatId, "ITEM_SPRINT");

        List<String> sprintOptions = sprints.stream()
                .map(s -> s.sprintId() + " — " + s.name())
                .toList();
        BotHelper.sendMessageToTelegram(chatId,
                "Step 1/5 — Sprint:\nWhich sprint is this task for?",
                telegramClient, buildOptionsKeyboard(sprintOptions, true));
    }

    private void handleAddItemStep(long chatId, String telegramId, String text, String state) {
        CreateWorkItemRequest req = pendingItems.get(chatId);
        if (req == null) {
            userState.remove(chatId);
            handleAddItemStart(chatId, telegramId);
            return;
        }

        switch (state) {
            case "ITEM_SPRINT" -> {
                // Text format: "sprint-id — Sprint Name", extract the id before " — "
                String sprintId = text.contains(" — ") ? text.split(" — ")[0].trim() : text.trim();
                List<SprintResponse> sprints = sprintService.findAll();
                boolean valid = sprints.stream().anyMatch(s -> s.sprintId().equals(sprintId));
                if (!valid) {
                    List<String> opts = sprints.stream().map(s -> s.sprintId() + " — " + s.name()).toList();
                    BotHelper.sendMessageToTelegram(chatId, "Please select a sprint from the list.",
                            telegramClient, buildOptionsKeyboard(opts, true));
                    return;
                }
                req.setSprintId(sprintId);
                userState.put(chatId, "ITEM_TITLE");
                BotHelper.sendMessageToTelegram(chatId, "Step 2/5 — Title:\nWhat is the title of the task?", telegramClient);
            }
            case "ITEM_TITLE" -> {
                req.setTitle(text);
                userState.put(chatId, "ITEM_DESCRIPTION");
                BotHelper.sendMessageToTelegram(chatId,
                        "Step 3/5 — Description (optional):\nAdd a description or send " + SKIP + " to skip.",
                        telegramClient);
            }
            case "ITEM_DESCRIPTION" -> {
                if (!text.equalsIgnoreCase(SKIP)) {
                    req.setDescription(text);
                }
                userState.put(chatId, "ITEM_TYPE");
                BotHelper.sendMessageToTelegram(chatId,
                        "Step 4/5 — Type:\nWhat type of work is this?",
                        telegramClient, buildOptionsKeyboard(List.of("TASK", "FEATURE", "BUG", "ISSUE"), true));
            }
            case "ITEM_TYPE" -> {
                String type = text.toUpperCase();
                if (!List.of("TASK", "FEATURE", "BUG", "ISSUE").contains(type)) {
                    BotHelper.sendMessageToTelegram(chatId, "Please select a valid type.",
                            telegramClient, buildOptionsKeyboard(List.of("TASK", "FEATURE", "BUG", "ISSUE"), true));
                    return;
                }
                req.setWorkType(type);
                userState.put(chatId, "ITEM_PRIORITY");
                BotHelper.sendMessageToTelegram(chatId,
                        "Step 5/5 — Priority:",
                        telegramClient, buildOptionsKeyboard(List.of("LOW", "MEDIUM", "HIGH"), true));
            }
            case "ITEM_PRIORITY" -> {
                WorkItemPriority priority;
                try {
                    priority = WorkItemPriority.valueOf(text.toUpperCase());
                } catch (IllegalArgumentException e) {
                    BotHelper.sendMessageToTelegram(chatId, "Please select a valid priority.",
                            telegramClient, buildOptionsKeyboard(List.of("LOW", "MEDIUM", "HIGH"), true));
                    return;
                }
                req.setPriority(priority);
                userState.put(chatId, "ITEM_MINUTES");
                BotHelper.sendMessageToTelegram(chatId,
                        "Estimated minutes (optional):\nEnter a number (e.g. 60) or send " + SKIP + " to skip.",
                        telegramClient);
            }
            case "ITEM_MINUTES" -> {
                if (!text.equalsIgnoreCase(SKIP)) {
                    try {
                        req.setEstimatedMinutes(Integer.parseInt(text));
                    } catch (NumberFormatException e) {
                        BotHelper.sendMessageToTelegram(chatId, "Please enter a valid number or send " + SKIP + " to skip.", telegramClient);
                        return;
                    }
                }
                // All done — create the item
                userState.remove(chatId);
                pendingItems.remove(chatId);
                workItemService.createWorkItem(req);

                String summary = "Task created!\n\n" +
                        "Title: " + req.getTitle() + "\n" +
                        "Type: " + req.getWorkType() + "\n" +
                        "Priority: " + req.getPriority() + "\n" +
                        (req.getEstimatedMinutes() != null ? "Estimated: " + req.getEstimatedMinutes() + " min\n" : "");
                BotHelper.sendMessageToTelegram(chatId, summary, telegramClient, buildMainKeyboard());
            }
        }
    }

    // --- LLM ---

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

    // --- Keyboards ---

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

    private ReplyKeyboardMarkup buildOptionsKeyboard(List<String> options, boolean oneTime) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < options.size(); i++) {
            row.add(options.get(i));
            if (row.size() == 2 || i == options.size() - 1) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(oneTime)
                .build();
    }

    private ReplyKeyboardMarkup buildUserSelectionKeyboard(List<AppUserSummary> users) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < users.size(); i++) {
            row.add(users.get(i).name());
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
