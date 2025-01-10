package Controller;

import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import DAO.StorageException;
import Model.Account;
import Model.Message;
import Service.SocialMediaService;
import Service.SocialMediaService.LoginException;
import Service.SocialMediaService.RegisterAccountException;
import Service.SocialMediaService.SendMessageException;
import Service.SocialMediaService.UpdateMessageException;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Optional;
import java.util.List;

public class SocialMediaController {
    private final SocialMediaService service = new SocialMediaService();
    private final ObjectMapper om = new ObjectMapper();

    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin builder = Javalin.create();
        builder.post("messages", this::handlePostMessage);
        builder.post("register", this::handlePostRegister);
        builder.post("login", this::handlePostLogin);
        builder.get("messages", this::handleGetMessages);
        builder.get("messages/{id}", this::handleGetMessageId);
        builder.delete("messages/{id}", this::handleDeleteMessageId);
        builder.patch("messages/{message_id}", this::handlePatchMessage);
        builder.get("accounts/{account_id}/messages", this::handleGetAccountMessages);
        return builder;
    }

    private void handlePatchMessage(final Context context) {
        final int message_id;
        try {
            message_id = Integer.valueOf(context.pathParam("message_id"));
        } catch (NumberFormatException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }

        final String message_text;
        
        try {
            message_text = om.readValue(context.body(), Message.class).message_text;
        } catch (JsonProcessingException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }

        final Message message;
        try {
            message = this.service.updateMessageText(message_id, message_text);
        } catch (UpdateMessageException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }

        context.json(message);
    }

    private void handleGetAccountMessages(final Context context) {
        final int account_id;
        try {
            account_id = Integer.valueOf(context.pathParam("account_id"));
        } catch (NumberFormatException e) {
            return;
        }

        final List<Message> messages;
        try {
            messages = this.service.getMessagesByUser(account_id);
        } catch (StorageException e) {
            e.printStackTrace();
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }
        
        context.json(messages);
    }

    private void handleDeleteMessageId(final Context context) {
        final int id;
        try {
            id = Integer.valueOf(context.pathParam("id"));
        } catch (NumberFormatException e) {
            return;
        }

        final Optional<Message> deleted;

        try {
            deleted = this.service.deleteMessage(id);
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            e.printStackTrace();
            return;
        }

        if (deleted.isPresent()) {
            final Message message = deleted.get();
            context.json(message);
        }
    }

    private void handleGetMessageId(final Context context) {
        final int id;
        try {
            id = Integer.valueOf(context.pathParam("id"));
        } catch (NumberFormatException e) {
            return;
        }

        final Message message;
        try {
            message = this.service.getMessageById(id).orElse(null);
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            e.printStackTrace();
            return;
        }

        if (message == null) return;

        context.json(message);
    }

    private void handleGetMessages(final Context context) {
        try {
            context.json(this.service.getAllMessages());
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }

    }

    private void handlePostLogin(final Context context) {
        final Account account;
        try {
            account = om.readValue(context.body(), Account.class);
        } catch (JsonProcessingException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }

        final int account_id;
        try {
            account_id = this.service.login(account.username, account.password);
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        } catch (LoginException e) {
            context.status(HttpStatus.UNAUTHORIZED_401);
            return;
        }
 
        account.account_id = account_id;

        context.json(account);
    }

    private void handlePostMessage(final Context context) {
        final Message message;
        try {
            message = om.readValue(context.body(), Message.class);
        } catch (JsonProcessingException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }

        final int message_id;
        try {
            message_id = this.service.sendMessage(message.posted_by, message.message_text,
                message.time_posted_epoch);
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        } catch (SendMessageException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
 
        message.message_id = message_id;

        context.json(message);
    }

    public void handlePostRegister(final Context context) {
        final Account account;
        try {
            account = om.readValue(context.body(), Account.class);
        } catch (JsonProcessingException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }

        final int account_id;
        try {
            account_id = this.service.registerAccount(account.username, account.password);
        } catch (StorageException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            e.printStackTrace();
            return;
        } catch (RegisterAccountException e) {
            context.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
 
        account.account_id = account_id;

        context.json(account);
    }

}