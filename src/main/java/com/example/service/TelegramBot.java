package com.example.service;

import com.example.config.BotConfig;
import com.example.entity.CourseEntity;
import com.example.entity.CourseStudentEntity;
import com.example.entity.StudentEntity;
import com.example.enums.AdminStep;
import com.example.enums.Role;
import com.example.enums.UserStep;
import com.example.utils.MD5;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;

    private final UsersService usersService;

    private final StudentService studentService;

    private final UserHistoryService userHistoryService;

    private final AuthService authService;

    private final StudentChatService studentChatService;

    private final AdminHistoryService adminHistoryService;

    private final CourseService courseService;

    private final CourseStudentService courseStudentService;

    @Value("${channel.id}")
    private Long channelId;

    public TelegramBot(BotConfig config, UsersService usersService, StudentService studentService, UserHistoryService userHistoryService, AuthService authService, StudentChatService studentChatService, AdminHistoryService adminHistoryService, CourseService courseService, CourseStudentService courseStudentService) {
        this.config = config;
        this.usersService = usersService;
        this.studentService = studentService;
        this.userHistoryService = userHistoryService;
        this.authService = authService;
        this.studentChatService = studentChatService;
        this.adminHistoryService = adminHistoryService;
        this.courseService = courseService;
        this.courseStudentService = courseStudentService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Boshlash"));
        listOfCommands.add(new BotCommand("/help", "Bot haqida ma'lumot"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getChat().getType().equals("supergroup")) {
                // DO NOTHING CHANNEL CHAT ID IS -1001764816733
                return;
            } else {
                Role role = usersService.getRoleByChatId(chatId);

                if (update.hasMessage() && update.getMessage().hasText()) {
                    String messageText = update.getMessage().getText();

                    if (messageText.startsWith("/")) {
                        if (messageText.startsWith("/login ")) {
                            String password = messageText.substring(7);

                            if (password.equals("Xp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6w9z$C&F)J@NcRfUjXn2r4u7x!A%D*G-Ka")) {
                                usersService.changeRole(chatId, Role.ROLE_ADMIN);
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            return;
                        }

                        switch (messageText) {
                            case "/start" -> {
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            case "/help" -> {
                                helpCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                                return;
                            }
                            default -> {
                                sendMessage(chatId, "Sorry, command was not recognized");
                                return;
                            }
                        }
                    }

                    if (role.equals(Role.ROLE_ADMIN)) {
                        if (messageText.equals("\uD83D\uDD1D Asosiy Menyu")) {
                            startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                            return;
                        }

                        if (messageText.equals("Students")) {
                            sendStudentsInfo(chatId);
                            adminHistoryService.create(chatId, AdminStep.STUDENTS_OPENED, "EMPTY");
                            return;
                        }
                        else if (messageText.equals("Courses")) {
                            sendCoursesInfo(chatId);
                            return;
                        }

                        AdminStep lastOpenedStep = adminHistoryService.getLastOpenedStep(chatId);

                        if (messageText.equals("\uD83D\uDD19 Cancel")) {
                            if (lastOpenedStep.equals(AdminStep.COURSES_OPENED) || lastOpenedStep.equals(AdminStep.COURSE_OPENED)) {
                                adminHistoryService.create(chatId, AdminStep.TRANSACTION_CANCELED, "EMPTY");
                                sendCoursesInfo(chatId);
                            } else if (lastOpenedStep.equals(AdminStep.STUDENTS_OPENED)) {
                                adminHistoryService.create(chatId, AdminStep.STUDENT_CREATING_CANCELED, "EMPTY");
                                sendStudentsInfo(chatId);
                            }

                            return;
                        }

                        if (messageText.equals("\uD83D\uDD19 Orqaga")) {
                            if (lastOpenedStep.equals(AdminStep.COURSES_OPENED) || lastOpenedStep.equals(AdminStep.COURSE_OPENED)) {
                                sendCoursesInfo(chatId);
                            } else if (lastOpenedStep.equals(AdminStep.STUDENTS_OPENED)) {
                                sendStudentsInfo(chatId);
                            }

                            return;
                        }

                        AdminStep lastStep = adminHistoryService.getLastStepByAdminId(chatId);
                        if (messageText.equals("➕")) {
                            if (lastOpenedStep.equals(AdminStep.COURSES_OPENED)) {
                                adminHistoryService.create(chatId, AdminStep.COURSES_NAME_ASKING, "EMPTY");
                                sendMessage(chatId, "What is course name?", "\uD83D\uDD19 Cancel");
                                return;
                            }

                            adminHistoryService.create(chatId, AdminStep.STUDENT_FIRST_NAME_ASKING, "EMPTY");
                            sendMessage(chatId, "What is student's first name?", "\uD83D\uDD19 Cancel");
                            return;
                        }
                        else if (lastStep != null) {
                            if (lastStep.equals(AdminStep.STUDENT_FIRST_NAME_ASKING)) {
                                adminHistoryService.create(chatId, AdminStep.STUDENT_FIRST_NAME_ASKED, messageText);
                                adminHistoryService.create(chatId, AdminStep.STUDENT_LAST_NAME_ASKING, "EMPTY");
                                sendMessage(chatId, "What is student's last name?");
                                return;
                            }
                            else if (lastStep.equals(AdminStep.STUDENT_LAST_NAME_ASKING)) {
                                adminHistoryService.create(chatId, AdminStep.STUDENT_LAST_NAME_ASKED, messageText);
                                sendMessage(chatId, "Enter the password to this student?");
                                adminHistoryService.create(chatId, AdminStep.STUDENT_PASSWORD_ASKING, "EMPTY");
                                return;
                            }
                            else if (lastStep.equals(AdminStep.STUDENT_PASSWORD_ASKING)) {
                                Long newStudentId = studentService.create(
                                        adminHistoryService.getLastValueByStep(chatId, AdminStep.STUDENT_FIRST_NAME_ASKED),
                                        adminHistoryService.getLastValueByStep(chatId, AdminStep.STUDENT_LAST_NAME_ASKED),
                                        MD5.encode(messageText)
                                );

                                adminHistoryService.create(chatId, AdminStep.STUDENT_PASSWORD_ASKED, messageText);
                                sendMessage(chatId, "<b>Successfully created ✅</b>\nStudent id: " + "<code>" + newStudentId + "</code>");
                                sendStudentsInfo(chatId);
                                return;
                            }
                            else if (lastStep.equals(AdminStep.COURSES_NAME_ASKING)) {
                                Long newCourseId = courseService.create(messageText);
                                adminHistoryService.create(chatId, AdminStep.COURSES_NAME_ASKED, messageText);
                                sendMessage(chatId, "<b>Successfully created ✅</b>\nCourse id: " + "<code>" + newCourseId + "</code>");
                                sendCoursesInfo(chatId);

                                return;
                            }
                            else if (lastStep.equals(AdminStep.COURSE_DELETING)) {
                                try {
                                    Boolean result = courseService.deleteById(Long.valueOf(messageText));
                                    if (!result) {
                                        sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                                        return;
                                    }
                                } catch (NumberFormatException e) {
                                    sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                                    return;
                                }
                                adminHistoryService.create(chatId, AdminStep.COURSE_DELETED, messageText);
                                sendMessage(chatId, "<b>Successfully deleted ✅</b> ", "\uD83D\uDD19 Cancel");
                                sendCoursesInfo(chatId);
                                return;
                            }
                            else if (lastStep.equals(AdminStep.COURSES_OPENED)) {
                                if (messageText.equals("\uD83D\uDDD1")) {
                                    adminHistoryService.create(chatId, AdminStep.COURSE_DELETING, "EMPTY");
                                    sendMessage(chatId, "Please send a Course-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                                    return;
                                }

                                sendStudentsInfoInCourse(chatId, messageText);
                                adminHistoryService.create(chatId, AdminStep.COURSE_OPENED, messageText);
                                return;
                            }
                            if (lastOpenedStep.equals(AdminStep.COURSE_OPENED)) {
                                if (messageText.equals("➕ \uD83E\uDDD1\u200D\uD83C\uDF93")) {
                                    sendMessage(chatId, "Please send a Student-ID which you want to add", "\uD83D\uDD19 Cancel");
                                    adminHistoryService.create(chatId, AdminStep.STUDENT_ADDING_TO_COURSE, "EMPTY");
                                }
                                else if (lastStep.equals(AdminStep.STUDENT_ADDING_TO_COURSE)) {
                                    String studentId = messageText;
                                    String courseName = adminHistoryService.getLastValueByStep(chatId, AdminStep.COURSE_OPENED);

                                    /* CHECK STUDENT EXISTS */
                                    try {
                                        StudentEntity byId = studentService.findById(Long.valueOf(studentId));
                                        if (byId == null) {
                                            sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to add ", "\uD83D\uDD19 Cancel");
                                            return;
                                        }
                                    } catch (NumberFormatException e) {
                                        sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to add ", "\uD83D\uDD19 Cancel");
                                        return;
                                    }

                                    courseStudentService.create(Long.valueOf(studentId), courseService.getCourseIdByName(courseName));
                                    sendMessage(chatId, "<b>Successfully added ✅</b>\nStudent id: " + "<code>" + studentId + "</code>");
                                    adminHistoryService.create(chatId, AdminStep.STUDENT_ADDED_TO_COURSE, "EMPTY");
                                    sendCoursesInfo(chatId);
                                }
                                else if (messageText.equals("➖ \uD83E\uDDD1\u200D\uD83C\uDF93")) {
                                    sendMessage(chatId, "Please send a Student-ID which you want to remove", "\uD83D\uDD19 Cancel");
                                    adminHistoryService.create(chatId, AdminStep.STUDENT_DELETING_FROM_COURSE, "EMPTY");
                                } else if (lastStep.equals(AdminStep.STUDENT_DELETING_FROM_COURSE)) {
                                    String studentId = messageText;
                                    String courseName = adminHistoryService.getLastValueByStep(chatId, AdminStep.COURSE_OPENED);

                                    try {
                                        StudentEntity byId = studentService.findById(Long.valueOf(studentId));
                                        if (byId == null) {
                                            sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to add ", "\uD83D\uDD19 Cancel");
                                            return;
                                        }
                                    }
                                    catch (NumberFormatException e) {
                                        sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to add ", "\uD83D\uDD19 Cancel");
                                        return;
                                    }

                                    courseStudentService.delete(Long.valueOf(studentId), courseService.getCourseIdByName(courseName));
                                    sendMessage(chatId, "<b>Successfully deleted ✅</b>");
                                    adminHistoryService.create(chatId, AdminStep.STUDENT_ADDED_TO_COURSE, "EMPTY");
                                    sendCoursesInfo(chatId);
                                }
                            }
                        }


                        if (messageText.equals("\uD83D\uDDD1")) {
                            if (lastOpenedStep.equals(AdminStep.STUDENTS_OPENED)) {
                                adminHistoryService.create(chatId, AdminStep.STUDENT_ID_ASKING, "EMPTY");
                                sendMessage(chatId, "Please send a Student-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                            }

                            return;
                        }
                        else if (lastStep != null) {
                            if (lastStep.equals(AdminStep.STUDENT_ID_ASKING)) {
                                try {
                                    Boolean result = studentService.delete(Long.valueOf(messageText));
                                    if (result) {
                                        adminHistoryService.create(chatId, AdminStep.STUDENT_ID_ASKED, messageText);
                                        sendMessage(chatId, "<b>Successfully deleted ✅</b>");
                                        sendStudentsInfo(chatId);
                                        return;
                                    }
                                    sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                                } catch (NumberFormatException e) {
                                    sendMessage(chatId, "<b>Student-ID not found!</b> \nPlease try to send another Student-ID which you want to remove ", "\uD83D\uDD19 Cancel");
                                }
                            }
                        }

                    }
                    else if (role.equals(Role.ROLE_STUDENT)) {
                        usersService.createUser(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());

                        if (messageText.equals("Total")) {

                        }
                    }
                    else if (role.equals(Role.ROLE_USER)) {
                        UserStep lastUserStep = userHistoryService.getLastStepByUserId(chatId);
                        if (lastUserStep.equals(UserStep.STUDENT_ID_ASKING)) {
                            StudentEntity student = studentService.findById(Long.valueOf(messageText));
                            if (student == null) {
                                sendMessage(chatId, "Student not found!");
                                return;
                            }
                            userHistoryService.create(chatId, UserStep.STUDENT_ID_ASKED, messageText);
                            sendMessage(chatId, "Enter your password: ");
                            userHistoryService.create(chatId, UserStep.STUDENT_PASSWORD_ASKING, "EMPTY");
                        } else if (lastUserStep.equals(UserStep.STUDENT_PASSWORD_ASKING)) {
                            Long studentId = Long.valueOf(userHistoryService.getLastValueByStep(chatId, UserStep.STUDENT_ID_ASKED));
                            userHistoryService.create(chatId, UserStep.STUDENT_PASSWORD_ASKED, messageText);
                            if (!authService.login(studentId, messageText)) {
                                sendMessage(chatId, "Incorrect password");
                                return;
                            }

                            usersService.changeRole(chatId, Role.ROLE_STUDENT);
                            studentChatService.create(studentId, chatId);

                            userHistoryService.create(chatId, UserStep.STUDENT_FOUND, String.valueOf(studentId));

                            startCommandReceived(
                                    chatId,
                                    update.getMessage().getChat().getFirstName(),
                                    update.getMessage().getChat().getLastName()
                            );
                        }
                    }
                }
            }

        }
    }


    private void startCommandReceived(long chatId, String firstName, String lastName) {
        Role role = usersService.createUser(chatId, firstName, lastName).getRole();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableHtml(true);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new LinkedList<>();
        markup.setKeyboard(keyboardRows);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);

        String textToSend = "";
        if (role.equals(Role.ROLE_USER)) {
            textToSend = "Welcome!\n" +
                    "Please, enter your Student-ID in order to use the bot\n" +
                    "e.g:2️⃣2️⃣0️⃣0️⃣0️⃣0️⃣  (only numbers)" +
                    "\n\n" +
                    "NOTE: Please for registration use only your Student-ID, not your friend's Student-ID or another it may pose a conflict with others\n" +
                    "Ps: You can update your Student-ID later and that's not a problem\n" +
                    "If you have any questions, please contact @Moxira_al";

            userHistoryService.create(chatId, UserStep.STUDENT_ID_ASKING, "EMPTY");

        } else if (role.equals(Role.ROLE_STUDENT)) {
            textToSend = "Welcome " + firstName + " " + lastName;

            KeyboardRow row = new KeyboardRow();
            row.add("Total");
            keyboardRows.add(row);
        } else if (role.equals(Role.ROLE_ADMIN)) {
            textToSend = "<b>Admin main menu</b>";

            KeyboardRow row = new KeyboardRow();
            row.add("Students");
            row.add("Courses");
            keyboardRows.add(row);
        }

        message.setText(textToSend);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Something went wrong in startCommandReceived()");
        }
    }

    private void sendStudentInfo(long chatId, long studentId) {

    }

    private void sendStudentsInfo(long chatId) {
        String textToSend = "<b>There are " + studentService.getCount() + " students.</b>";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new LinkedList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("➕");
        keyboardRow.add("\uD83D\uDDD1");

        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        keyboardRow.add("\uD83D\uDD1D Asosiy Menyu");

        keyboardRows.add(keyboardRow);

        markup.setKeyboard(keyboardRows);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
    private void sendCoursesInfo(long chatId) {
        String textToSend = "<b>There are " + courseService.getCount() + " courses.</b>";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new LinkedList<>();

        List<CourseEntity> all = courseService.findAll();
        KeyboardRow keyboardRow = new KeyboardRow();
        for (int i = 1; i <= all.size(); i++) {
            keyboardRow.add(all.get(i-1).getName());

            if (i % 2 == 0) {
                keyboardRows.add(keyboardRow);
                keyboardRow = new KeyboardRow();
            } else if (i == all.size()) {
                keyboardRows.add(keyboardRow);
            }
        }

        keyboardRow = new KeyboardRow();
        keyboardRow.add("➕");
        keyboardRow.add("\uD83D\uDDD1");

        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        keyboardRow.add("\uD83D\uDD1D Asosiy Menyu");

        keyboardRows.add(keyboardRow);

        markup.setKeyboard(keyboardRows);

        message.setReplyMarkup(markup);

        adminHistoryService.create(chatId, AdminStep.COURSES_OPENED, "EMPTY");

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void sendStudentsInfoInCourse(long chatId, String courseName) {
        try {
            Long courseId = courseService.getCourseIdByName(courseName);
            List<CourseStudentEntity> courseInfo = courseStudentService.getCourseInfo(courseId);

            int j = 1;
            StringBuilder stringBuilder = new StringBuilder("<b>There are "
                            + courseInfo.size()
                            + " students are studying in this course.</b> (Course Id: <code>"
                            + courseId + "</code>)\n");
            for (CourseStudentEntity courseStudent : courseInfo) {
                StudentEntity student = studentService.findById(courseStudent.getStudentId());
                stringBuilder
                        .append(j + ". ")
                        .append(student.getFirstName())
                        .append(" ")
                        .append(student.getLastName())
                        .append(" (Student Id: <code>" + student.getId() + "</code>)" + " ")
                        .append("\n");
                j++;
            }

            String message = stringBuilder.toString();
            if (message.length() > 1000) {
                for (int i = 1000; i < message.length(); i+=1000) {
                    String messageCast = message.substring(100);
                    sendMessage(chatId, messageCast);
                }

                return;
            }

            sendMessage(chatId, message, "➕ \uD83E\uDDD1\u200D\uD83C\uDF93", "➖ \uD83E\uDDD1\u200D\uD83C\uDF93", "\uD83D\uDD19 Orqaga");
        } catch (NoSuchElementException e) {
            log.error("Something went wrong in sendStudentsInfoInCourse() method");
        }
    }

    private void helpCommandReceived(long chatId, String name) {
        sendMessage(chatId, "Assalomu aleykum, " + name + ". \nSiz bu bot orqali o'zingizga kerakli bo'lgan bo'limlardan foydalanishingiz, buyurtma berishingiz, va o'z xizmatingizni taklif etishingiz mumkin. ");
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void sendMessage(long chatId, String textToSend, String... buttonTexts) {
        if (buttonTexts.length > 4) {
            return;
        }

        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new LinkedList<>();
        markup.setKeyboard(keyboardRows);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();

        for (String buttonText : buttonTexts) {
            keyboardRow.add(buttonText);
        }

        keyboardRows.add(keyboardRow);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
}
