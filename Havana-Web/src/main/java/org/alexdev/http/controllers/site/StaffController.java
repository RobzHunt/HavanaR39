package org.alexdev.http.controllers.site;

import org.alexdev.http.server.HttpRequest;
import org.alexdev.http.server.HttpResponse;
import org.alexdev.http.template.Template;
import org.alexdev.habbo.Habbo;

import java.util.*;

public class StaffController {

    public static void handle(HttpRequest request, HttpResponse response) {

        Template tpl = request.template("community_staff");

        List<Map<String, Object>> staffList = new ArrayList<>();

        for (var user : Habbo.getUsers().getOnlineUsers().values()) {

            if (user.getRank().getId() >= 4) {

                Map<String, Object> u = new HashMap<>();

                u.put("username", user.getUsername());
                u.put("motto", user.getMotto());
                u.put("look", user.getLook());
                u.put("rank", user.getRank().getId());

                boolean online = user.getClient() != null;
                u.put("online", online ? "on" : "off");
                u.put("online_text", online ? "Online" : "Offline");

                // 🎖️ Rank mapping
                switch (user.getRank().getId()) {
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
        }

        tpl.set("staff", staffList);
        tpl.render(response);
    }
}
