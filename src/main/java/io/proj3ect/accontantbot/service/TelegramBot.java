package io.proj3ect.accontantbot.service;

import io.proj3ect.accontantbot.config.TelegramBotConfig;
import io.proj3ect.accontantbot.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.security.auth.callback.Callback;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TelegramBot extends TelegramLongPollingBot {
    //Автоматический то есть Spring сам авторизует репозиторий(базу данных)
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpendRepository spendRepository;
    @Autowired
    private EarnRepository earnRepository;
    @Autowired
    private MailNowRepository mailNowRepository;
    //Текст для функции help

    final static String Help_text="I am your personal AccontanatBot, I can record your financial expenses and income.\n" +
            "If you want record information about expenses, just write or take from menu '/spend'\n" +
            "or if you want record information abot income, write '/earn'.\n" +
            "Also you can watch your history with '/history',\n or watch how many you spend or earn with '/sum'.\n" +
            "Or you can delete all your information with /delete.\nI hope I will be benefit for you.";
    private Map<Long, BotState> userStates = new HashMap<>();
    //В этом конфиге хранится вся информация о боте
    @Autowired
    final TelegramBotConfig botConfig;
    //Конструктор
    public TelegramBot(TelegramBotConfig botConfig){
        this.botConfig=botConfig;
        //Добавление меню в чате сперва листом вклюсил все BotCommand это тип данных для команд
        List<BotCommand> listofCommands=new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/spend", "set a information about spend money"));
        listofCommands.add(new BotCommand("/earn", "set a information about earn money"));
        listofCommands.add(new BotCommand("/history", "get a information about spend and earn"));
        listofCommands.add(new BotCommand("/sum", "get sum about all money"));
        listofCommands.add(new BotCommand("/delete", "delete all information"));
        listofCommands.add(new BotCommand("/help", "info how to use bot"));
        try{
            //Меняет меню, чтобы туда попали вещи из списка
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("We have problem "+e.getMessage());
        }
    }
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    //Весь изменяемый код сюда то есть вся работа здесь
    @RequestMapping(value = "/webhook", method = RequestMethod.POST)
    public void onUpdateReceived(Update update) {
        // проверяет еслть сообщение и в сообщение есть текст ведь есть еще CallBack
        if(update.hasMessage() && update.getMessage().hasText()){
            //получил сообщение
            String msgText=update.getMessage().getText();
            //получил айди чата
            long ChatId=update.getMessage().getChatId();
            //проверка всех сообщений
            if(msgText.contains("/start")) startCommand(ChatId, update.getMessage().getChat().getFirstName());
            else if(msgText.contains("/spend")){
                // Установите состояние для пользователя, чтобы помнить, что следующий ввод ожидается в ответ на вопрос об потраченной сумме.
                setUserState(ChatId, BotState.WAITING_FOR_SPEND);
                sendMessage(ChatId, "How many do you spend?");

            } else if (msgText.contains(("/earn"))) {
                setUserState(ChatId, BotState.WAITING_FOR_EARN);
                sendMessage(ChatId, "How many do you earn?");
            } else if (msgText.equals("/help")) {
                sendMessage(ChatId, Help_text);
            } else if (msgText.equals("/history")) {
                sendHistory(ChatId);

            }
            else if(msgText.equals("/delete")){
                delete_date(ChatId);
            }
            else if(msgText.contains(("/sent")) && ChatId==botConfig.getBotHostId()){
                String SentMet = msgText.substring(msgText.indexOf(" "));
                var Users= userRepository.findAll();
                for(User user:Users){
                    sendMessage(user.getId(), SentMet);
                }
            }
            else if(msgText.equals("/sum")){
                sentSum(ChatId);
            } else{
                var mailnow=mailNowRepository.findAll();
                for (MailNow mailNow1:mailnow) {
                        var Users= userRepository.findAll();
                        for(User user:Users){
                            if(mailNow1.getDoc().equals("Yes")){
                                Path path = Paths.get(mailNow1.getDocPath());
                                // Создание объекта SendDocument
                                SendDocument sendDocument = new SendDocument();
                                sendDocument.setChatId(String.valueOf(user.getId()));  // chatId пользователя, которому вы хотите отправить документ
                                sendDocument.setDocument(new InputFile(path.toFile()));
                                sendDocument.setCaption(mailNow1.getBody());
                                try{
                                    execute(sendDocument);
                                }catch(TelegramApiException e){
                                    System.out.println("We have problem"+e.getMessage());
                                }
                            } else if (mailNow1.getPhoto().equals("Yes")) {
                                Path path = Paths.get(mailNow1.getPicPath());
                                SendPhoto sendPhoto=new SendPhoto();
                                sendPhoto.setChatId(String.valueOf(user.getId()));
                                sendPhoto.setPhoto(new InputFile(path.toFile()));
                                sendPhoto.setCaption(mailNow1.getBody());
                                try{
                                    execute(sendPhoto);
                                }catch(TelegramApiException e){
                                    System.out.println("We have problem"+e.getMessage());
                                }
                            }
                            else{
                                sendMessage(user.getId(), mailNow1.getBody());
                            }
                        }
                        mailNowRepository.deleteById(mailNow1.getId());
                    }


                processUserInput(ChatId, msgText);

            }

        }
        else if(update.hasCallbackQuery()){
            String callBack=update.getCallbackQuery().getData();
            long messageId=update.getCallbackQuery().getMessage().getMessageId();
            long chatId=update.getCallbackQuery().getMessage().getChatId();
            String text="";
            if(callBack.equals("Yes_Delete")){
                text="All your data was cleaned(deleted)!";
                var User_spend=spendRepository.findAll();
                var User_earn=earnRepository.findAll();
                for(Spend user:User_spend){
                    if(user.getChatId()==chatId){
                        spendRepository.deleteById((long)user.getId());
                    }
                }
                for(Earn user:User_earn){
                    if(user.getChatid()==chatId){
                        earnRepository.deleteById((long)user.getId());
                    }
                }
            }
            else if(callBack.equals("No_Delete")){
                text="OK, your date isn't deleted";
            }
            EditMessageText messageText=new EditMessageText();
            messageText.setChatId(String.valueOf(chatId));
            messageText.setText(text);
            messageText.setMessageId((int) messageId);
            try{
                execute(messageText);
            }catch(TelegramApiException e){
                System.out.println("We have problem "+e.getMessage());
            }
        }
    }
    //Метод для сообщение старт
    private void startCommand(long ChatId, String name){
        String text="Hi, "+name+", nice to meet you! My name is AccountantBot," +
                " I am your personal accountant, I can" +
                " record your expenses or income. Let's start";
        sendMessage(ChatId, text);
        register_user(ChatId, name);
    }
    //метод который отправляет сообщение, для отправки используется execute
    private void sendMessage(long ChatId, String text){
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(String.valueOf(ChatId));
        sendMessage.setText(text);
        try{
            execute(sendMessage);
        }catch(TelegramApiException e){
            System.out.println("We have problem "+e.getMessage());
        }

    }
    //В базу данных добавляються данные все траты
    private void register_spend(double money, long ChatId){
        Spend users=new Spend();
        users.setChatId(ChatId);
        users.setSpend_money(money);
        users.setTime(new Timestamp(System.currentTimeMillis()));
        spendRepository.save(users);
    }
    //В базу данных добавляються данные все доходы
    private void register_earn(double money, long ChatId){
        Earn earns=new Earn();
        earns.setChatid(ChatId);
        earns.setSpend_money(money);
        earns.setTime(new Timestamp(System.currentTimeMillis()));
        earnRepository.save(earns);
    }
    private void register_user(long ChatId, String name){
        User users=new User();
        users.setId(ChatId);
        users.setName(name);
        users.setTime(new Timestamp(System.currentTimeMillis()));
        userRepository.save(users);
    }


    private void delete_date(long ChatId){
        SendMessage sendMsg=new SendMessage();
        sendMsg.setChatId(String.valueOf(ChatId));
        sendMsg.setText("Are you sure? All dates will delete!");
        InlineKeyboardMarkup inKey=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline=new ArrayList<>();
        List<InlineKeyboardButton> rowInline=new ArrayList<>();
        var yesbutton=new InlineKeyboardButton();
        yesbutton.setText("Yes");
        yesbutton.setCallbackData("Yes_Delete");
        var nobutton=new InlineKeyboardButton();
        nobutton.setText("No");
        nobutton.setCallbackData("No_Delete");
        rowInline.add(yesbutton);
        rowInline.add(nobutton);
        rowsInline.add(rowInline);
        inKey.setKeyboard(rowsInline);
        sendMsg.setReplyMarkup(inKey);
        try{
            execute(sendMsg);
        }catch(TelegramApiException e){
            System.out.println("We have problem "+e.getMessage());
        }
    }

    private void sendHistory(long ChatId){
        var User_Spend=spendRepository.findAll();
        var User_Earn=earnRepository.findAll();
        String history="You spend: \n";
        double summa=0;
        Timestamp one;

        for(Spend user:User_Spend){
            if(user.getChatId()==ChatId){
                summa = user.getSpend_money();
                one=user.getTime();
                history+=summa+" at time "+one+" \n";
            }
        }
        history+="You earn: \n";
        for(Earn user:User_Earn){
            if(user.getChatid()==ChatId){
                summa = user.getSpend_money();
                one=user.getTime();
                history+=summa+" at time "+one+" \n";
            }
        }
        sendMessage(ChatId, history);
    }

    private void sentSum(long ChatId){
        var User_Spend=spendRepository.findAll();
        var User_Earn=earnRepository.findAll();

        double summa_spend=0;
        double summa_earn=0;


        for(Spend user:User_Spend){
            if(user.getChatId()==ChatId){
                summa_spend += user.getSpend_money();
            }
        }
        for(Earn user:User_Earn){
            if(user.getChatid()==ChatId){
                summa_earn += user.getSpend_money();
            }
        }
        String sum="You spend all: "+summa_spend+"\n You earn all: "+summa_earn+" \n" +
                "Your balance is "+(summa_earn-summa_spend)+" ";
        sendMessage(ChatId, sum);
    }


    private void processUserInput(long chatId, String messageText) {
        BotState currentState = getUserState(chatId);

        switch (currentState) {
            case WAITING_FOR_SPEND:
                // Обработайте введенную сумму.
                processSpendInput(chatId, messageText);
                break;
            // Добавьте другие состояния и их обработку по мере необходимости.
            case WAITING_FOR_EARN:
                // Обработайте введенную сумму.
                processEarnInput(chatId, messageText);
                break;
            default:
                String Idk="I dont know this function";
                sendMessage(chatId, Idk);
                break;

        }
    }
    private void processSpendInput(long ChatId, String msgText) {
        // Обработайте введенную сумму, например, сохраните ее в базу данных.
        // В данном примере, просто отправим пользователю сообщение с подтверждением.
        try {
            double How_many = Double.parseDouble(msgText);
            register_spend(How_many, ChatId);
            String text = "Your information entered in base";
            sendMessage(ChatId, text);

        }catch (Exception e){
            sendMessage(ChatId, "Pls enter numbers");
        }
        // Сбросьте состояние пользователя после завершения операции.
        resetUserState(ChatId);
    }
    private void processEarnInput(long ChatId, String msgText) {
        // Обработайте введенную сумму, например, сохраните ее в базу данных.
        // В данном примере, просто отправим пользователю сообщение с подтверждением.
        try {
            double How_many = Double.parseDouble(msgText);
            register_earn(How_many, ChatId);
            String text = "Your information entered in base";
            sendMessage(ChatId, text);

        }catch (Exception e){
            sendMessage(ChatId, "Pls enter numbers");
        }
        // Сбросьте состояние пользователя после завершения операции.
        resetUserState(ChatId);
    }
    private void setUserState(long chatId, BotState state) {
        userStates.put(chatId, state);
    }
    private BotState getUserState(long chatId) {
        return userStates.getOrDefault(chatId, BotState.DEFAULT);
    }

    private void resetUserState(long chatId) {
        userStates.remove(chatId);
    }
}
