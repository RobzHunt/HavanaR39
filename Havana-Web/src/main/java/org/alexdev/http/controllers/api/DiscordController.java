/**
 * Author: Parsnip#5170
 */

package org.alexdev.http.controllers.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.alexdev.duckhttpd.server.connection.WebConnection;
import org.alexdev.havana.dao.mysql.PlayerDao;
import org.alexdev.havana.game.player.PlayerDetails;
import org.alexdev.havana.server.rcon.RconConnectionHandler;
import org.alexdev.havana.server.rcon.messages.RconHeader;
import org.alexdev.havana.util.config.GameConfiguration;
import org.alexdev.http.util.RconUtil;
import org.alexdev.http.util.SessionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DiscordController {

    private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    private static final String USER_URL  = "https://discordapp.com/api/users/@me";

    public static void verify(WebConnection webConnection) {
        var code = webConnection.get().getString("code");

        try {
            var accessToken = fetchAccessToken(code);
            var discordId   = fetchDiscordId(accessToken);

            handleVerification(webConnection, discordId);

        } catch (Exception e) {
            webConnection.session().set("alertMessage", "Something went wrong\n");
            webConnection.redirect("/");
        }
    }

    // ---------------------------------------------------------
    // Troca o code pelo access_token
    // ---------------------------------------------------------
    private static String fetchAccessToken(String code) throws Exception {
        var clientId     = GameConfiguration.getInstance().getString("discord.client_id");
        var clientSecret = GameConfiguration.getInstance().getString("discord.client_secret");
        var redirectUri  = GameConfiguration.getInstance().getString("site.path") + "/api/discord";

        var body =
            "client_id="     + URLEncoder.encode(clientId,               "UTF-8") + "&" +
            "client_secret=" + URLEncoder.encode(clientSecret,           "UTF-8") + "&" +
            "grant_type="    + URLEncoder.encode("authorization_code",   "UTF-8") + "&" +
            "code="          + URLEncoder.encode(code,                   "UTF-8") + "&" +
            "redirect_uri="  + URLEncoder.encode(redirectUri,            "UTF-8") + "&" +
            "scope="         + URLEncoder.encode("identify",             "UTF-8");

        var response = post(TOKEN_URL, body);
        return new ObjectMapper().readTree(response).get("access_token").asText();
    }

    // ---------------------------------------------------------
    // Busca o ID do usuário Discord com o token
    // ---------------------------------------------------------
    private static BigDecimal fetchDiscordId(String accessToken) throws Exception {
        var connection = (HttpURLConnection) new URL(USER_URL).openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (connection.getResponseCode() != 200) {
            throw new Exception("Discord user fetch failed: " + connection.getResponseCode());
        }

        var response = readResponse(connection);
        return new BigDecimal(new ObjectMapper().readTree(response).get("id").asText());
    }

    // ---------------------------------------------------------
    // Decide o fluxo: vincular conta ou fazer login
    // ---------------------------------------------------------
    private static void handleVerification(WebConnection webConnection, BigDecimal discordId) throws Exception {
        if (webConnection.session().getBoolean("authenticated")) {
            linkDiscordAccount(webConnection, discordId);
        } else {
            loginWithDiscord(webConnection, discordId);
        }
    }

    private static void linkDiscordAccount(WebConnection webConnection, BigDecimal discordId) {
        var existingUid = PlayerDao.getByDiscordId(discordId);

        if (existingUid > 0) {
            webConnection.session().set("alertMessage", "Your Discord account is already linked to another user\n");
        } else {
            var uid = webConnection.session().getInt("user.id");
            PlayerDao.setDiscordId(uid, discordId);
            webConnection.session().set("discord.saved.alert", true);
            RconUtil.sendCommand(RconHeader.REFRESH_TRADE_SETTING, new HashMap<>());
        }

        webConnection.redirect("/");
    }

    private static void loginWithDiscord(WebConnection webConnection, BigDecimal discordId) {
        var destination = SessionUtil.login(webConnection, discordId, true)
            ? "/security_check"
            : "/";

        webConnection.redirect(destination);
    }

    // ---------------------------------------------------------
    // Helpers HTTP genéricos
    // ---------------------------------------------------------
    private static String post(String targetUrl, String body) throws Exception {
        var connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
        connection.setDoOutput(true);

        try (var out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes(body);
        }

        if (connection.getResponseCode() != 200) {
            throw new Exception("HTTP POST failed: " + connection.getResponseCode());
        }

        return readResponse(connection);
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        var sb = new StringBuilder();

        try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        return sb.toString();
    }
}
