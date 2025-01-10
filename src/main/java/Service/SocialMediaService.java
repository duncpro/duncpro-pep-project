package Service;

import DAO.Dao;
import DAO.StorageException;
import Model.Account;
import Model.Message;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class SocialMediaService {
    private final Dao dao = new Dao();

    public static class SendMessageException extends Exception {}

    public int sendMessage(int posted_by, String message_text, long time_posted) throws StorageException, SendMessageException {
        if (message_text.isBlank()) throw new SendMessageException();
        if (message_text.length() > 255) throw new SendMessageException();
        if (!this.dao.isUser(posted_by)) throw new SendMessageException();
        return this.dao.insertMessage(posted_by, message_text, time_posted);
    }

    public static class RegisterAccountException extends Exception {}

    public int registerAccount(String username, String password) throws StorageException, RegisterAccountException {
        if (username.isBlank()) throw new RegisterAccountException();
        if (password.length() < 4) throw new RegisterAccountException();
        if (this.dao.isUsername(username)) throw new RegisterAccountException();
        return this.dao.insertAccount(username, password);
    }

    public static class LoginException extends Exception {}

    public int login(String username, String password) throws StorageException, LoginException {
        Optional<Account> result = this.dao.getUserByUsername(username);

        if (result.isEmpty()) throw new LoginException();

        Account account = result.get();

        if (!Objects.equals(account.password, password)) 
            throw new LoginException();

        return account.account_id;
    }

    public List<Message> getAllMessages() throws StorageException {
        return this.dao.getAllMessages();
    }

    public Optional<Message> getMessageById(int id) throws StorageException {
        return this.dao.getMessageById(id);
    }

    public Optional<Message> deleteMessage(int id) throws StorageException {
        final Optional<Message> message = this.getMessageById(id);
        this.dao.deleteMessage(id);
        return message;
    }

    public List<Message> getMessagesByUser(int accountId) throws StorageException {
        return this.dao.getMessagesByUser(accountId);
    }

    public static class UpdateMessageException extends Exception {}

    public Message updateMessageText(int messageId, String messageText) throws StorageException, UpdateMessageException {
        if (messageText.isBlank()) throw new UpdateMessageException();
        if (messageText.length() > 255) throw new UpdateMessageException();
        this.dao.updateMessageText(messageId, messageText);
        final Optional<Message> message = this.dao.getMessageById(messageId);
        if (message.isEmpty()) throw new UpdateMessageException();
        return message.get();
    }
}
