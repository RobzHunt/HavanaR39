package org.alexdev.http.controllers.site;

import org.alexdev.http.server.HttpRequest;
import org.alexdev.http.server.HttpResponse;
import org.alexdev.http.template.Template;
import org.alexdev.http.util.XSSUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class StaffController {

    public static void handle(HttpRequest request, HttpResponse response) {

        XSSUtil.clear(request);

        Template tpl = request.template("community_staff");

        List<Map<String, Object>> staffList = new ArrayList<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT username, motto, look, rank FROM users WHERE rank >= 4 ORDER BY rank DESC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> u = new HashMap<>();

                int rank = rs.getInt("rank");

                u.put("username", rs.getString("username"));
                u.put("motto", rs.getString("motto"));
                u.put("look", rs.getString("look"));
                u.put("rank", rank);

                // online check (opcional)
                u.put("online", "off");
                u.put("online_text", "Offline");

                switch (rank) {
                    case 7:
                        u.put("rank_name", "Fundador");
                        u.put("badge", "ADM");
                        u.put("color", "gold");
                        break;
                    case 6:
                        u.put("rank_name", "Administrador");
                        u.put("badge", "ADM");
                        u.put("color", "red");
                        break;
                    case 5:
                        u.put("rank_name", "Moderador");
                        u.put("badge", "MOD");
                        u.put("color", "blue");
                        break;
                    case 4:
                        u.put("rank_name", "Suporte");
                        u.put("badge", "HELP");
                        u.put("color", "green");
                        break;
                }

                staffList.add(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        tpl.set("staff", staffList);
        tpl.render(response);
    }
}
