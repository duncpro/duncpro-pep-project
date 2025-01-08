package Controller;

import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import DAO.Dao;
import DAO.StorageException;
import Model.Account;
import Model.Message;
import Service.SocialMediaService;
import Service.SocialMediaService.RegisterAccountException;
import Service.SocialMediaService.SendMessageException;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
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
        return builder;
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