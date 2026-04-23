package org.alexdev.http.dao;

import org.alexdev.havana.dao.Storage;
import org.alexdev.http.game.account.ClientPreference;

import java.sql.*;

public class SessionDao {

    // Helper interno para evitar repetição do try/finally
    @FunctionalInterface
    private interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }

    private static void execute(SqlConsumer action) {
        try (Connection conn = Storage.getStorage().getConnection()) {
            action.accept(conn);
        } catch (SQLException e) {
            Storage.logError(e);
        }
    }

    // -------------------------------------------------------

    public static int getRememberToken(String token) {
        int[] userId = {0};

        execute(conn -> {
            try (PreparedStatement ps = Storage.getStorage()
                    .prepare("SELECT id FROM users WHERE remember_token = ?", conn)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId[0] = rs.getInt("id");
                }
            }
        });

        return userId[0];
    }

    public static void setRememberToken(int userId, String token) {
        execute(conn -> {
            try (PreparedStatement ps = Storage.getStorage()
                    .prepare("UPDATE users SET remember_token = ? WHERE id = ?", conn)) {
                ps.setString(1, token);
                ps.setInt(2, userId);
                ps.execute();
            }
        });
    }

    public static void clearRememberToken(int userId) {
        execute(conn -> {
            try (PreparedStatement ps = Storage.getStorage()
                    .prepare("UPDATE users SET remember_token = ? WHERE id = ?", conn)) {
                ps.setNull(1, Types.VARCHAR);
                ps.setInt(2, userId);
                ps.execute();
            }
        });
    }

    // Parâmetros agrupados em objeto para melhorar legibilidade
    public static void savePreferences(UserPreferences prefs, int userId) {
        execute(conn -> {
            try (PreparedStatement ps = Storage.getStorage().prepare(
                    "UPDATE users SET motto = ?, profile_visible = ?, online_status_visible = ?, " +
                    "wordfilter_enabled = ?, allow_friend_requests = ?, allow_stalking = ?, " +
                    "client_preference = ?, hotel_view = ? WHERE id = ?", conn)) {
                ps.setString(1, prefs.motto());
                ps.setInt(2, prefs.profileVisible() ? 1 : 0);
                ps.setInt(3, prefs.showOnlineStatus() ? 1 : 0);
                ps.setInt(4, prefs.wordFilterEnabled() ? 1 : 0);
                ps.setInt(5, prefs.allowFriendRequests() ? 1 : 0);
                ps.setInt(6, prefs.allowStalking() ? 1 : 0);
                ps.setString(7, prefs.clientPreference());
                ps.setString(8, prefs.hotelView());
                ps.setInt(9, userId);
                ps.execute();
            }
        });
    }

    public static void saveTrade(int userId, boolean tradeSetting) {
        execute(conn -> {
            try (PreparedStatement ps = Storage.getStorage()
                    .prepare("UPDATE users SET trade_enabled = ? WHERE id = ?", conn)) {
                ps.setInt(1, tradeSetting ? 1 : 0);
                ps.setInt(2, userId);
                ps.execute();
            }
        });
    }
}
