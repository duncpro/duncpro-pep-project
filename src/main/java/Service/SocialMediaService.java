package Service;

import DAO.Dao;
import DAO.StorageException;
import Model.Account;

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
}
