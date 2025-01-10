package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import Model.Account;
import Model.Message;
import Util.ConnectionUtil;


public class Dao {
    public int updateMessageText(int message_id, String message_text) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "UPDATE message SET message_text = ? WHERE message_id = ?;")) {
                        statement.setString(1, message_text);
                        statement.setInt(2, message_id);
                        return statement.executeUpdate();
                    }
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public List<Message> getMessagesByUser(int accountId) throws StorageException {
        List<Message> messages = new ArrayList<Message>();

        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                "SELECT message_id, message_text, time_posted_epoch FROM message WHERE posted_by = ?;")) {
                    statement.setInt(1, accountId);

                    try (final ResultSet results = statement.executeQuery()) {
                        while (results.next()) {
                            final Message message = new Message();
                            message.message_id = results.getInt("message_id");
                            message.message_text = results.getString("message_text");
                            message.time_posted_epoch = results.getLong("time_posted_epoch");
                            message.posted_by = accountId;
                            messages.add(message);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        return messages;
    }

    public void deleteMessage(int id) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM message WHERE message_id = ?;")) {
                        statement.setInt(1, id);
                        statement.executeUpdate();
                    }
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public Optional<Message> getMessageById(int id) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "SELECT posted_by, message_text, time_posted_epoch FROM message WHERE message_id = ?;")) {
                        statement.setInt(1, id);
                        try (final ResultSet results = statement.executeQuery()) {
                            if (results.next()) {
                                final Message message = new Message();
                                message.message_id = id;
                                message.posted_by = results.getInt(1);
                                message.message_text = results.getString(2);
                                message.time_posted_epoch = results.getLong(3);
                                return Optional.of(message);
                            }
                        }
                }
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }
        return Optional.empty();
    }

    public List<Message> getAllMessages() throws StorageException {
        final List<Message> messages = new ArrayList<Message>();
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "SELECT message_id, posted_by, message_text, time_posted_epoch FROM message;")) {
                        try (final ResultSet results = statement.executeQuery()) {
                            while (results.next()) {
                                final Message message = new Message();
                                message.message_id = results.getInt(1);
                                message.posted_by = results.getInt(2);
                                message.message_text = results.getString(3);
                                message.time_posted_epoch = results.getLong(4);
                                messages.add(message);
                            }
                        }
                }
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }
        return messages;
    }

    public int insertAccount(final String username, final String password) 
    throws StorageException 
    {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO account (username, password) VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS))
                {
                    statement.setString(1, username);
                    statement.setString(2, password);
                    statement.executeUpdate();

                    try (final ResultSet results = statement.getGeneratedKeys()) {
                        results.first();
                        final int key = results.getInt(1);
                        if (results.next()) throw new AssertionError();
                        return key;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        }
    }

    public Optional<Account> getUserByUsername(final String username) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "SELECT account_id, password FROM account WHERE username = ?"))
                {
                    statement.setString(1, username);
                    try (final ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            return Optional.of(new Account(
                                result.getInt(1),
                                username,
                                result.getString(2)
                            ));
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        }

        return Optional.empty();
    }


    public boolean isUsername(final String username) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM account WHERE username = ?"))
                {
                    statement.setString(1, username);
                    try (final ResultSet result = statement.executeQuery()) {
                        result.first();
                        return result.getInt(1) > 0;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        }
    }

    public boolean isUser(int id) throws StorageException {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM account WHERE account_id = ?"))
                {
                    statement.setInt(1, id);
                    try (final ResultSet result = statement.executeQuery()) {
                        result.first();
                        return result.getInt(1) > 0;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        }
    }

    public int insertMessage(final int posted_by, final String message_text, final long time_posted_epoch) 
    throws StorageException 
    {
        try {
            try (final Connection connection = ConnectionUtil.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO message (message_text, time_posted_epoch, posted_by) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS))
                {
                    statement.setString(1, message_text);
                    statement.setLong(2, time_posted_epoch);
                    statement.setInt(3, posted_by);
                    statement.executeUpdate();

                    try (final ResultSet results = statement.getGeneratedKeys()) {
                        results.first();
                        final int key = results.getInt(1);
                        if (results.next()) throw new AssertionError();
                        return key;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        }
    }
}
