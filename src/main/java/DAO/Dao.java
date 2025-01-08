package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import Util.ConnectionUtil;

/**
 *     message_id int primary key auto_increment,
    posted_by int,
    message_text varchar(255),
    time_posted_epoch bigint,
    foreign key (posted_by) references  account(account_id)
 */

public class Dao {
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
