package org.alexdev.http.controllers;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.alexdev.duckhttpd.routes.Route;
import org.alexdev.duckhttpd.server.connection.WebConnection;
import org.alexdev.havana.util.DateUtil;
import org.alexdev.havana.util.config.GameConfiguration;
import org.alexdev.http.Routes;
import org.alexdev.http.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController implements Route {

    @Override
    public void handleRoute(WebConnection webConnection) throws Exception {
        if (!webConnection.isRequestHandled()) {
            return;
        }

        if (shouldRedirectToHttps(webConnection)) {
            redirectToHttps(webConnection);
            return;
        }

        if (isUnderMaintenance(webConnection)) {
            webConnection.redirect("/maintenance");
            return;
        }

        if (webConnection.session().getBoolean("authenticated")) {
            handleAuthenticatedRoute(webConnection);
        } else {
            SessionUtil.checkCookie(webConnection);
        }
    }

    // ---------------------------------------------------------

    private boolean shouldRedirectToHttps(WebConnection webConnection) {
        if (webConnection.getRouteRequest().startsWith("/api")) {
            return false;
        }

        var headers = webConnection.request().headers();

        if (headers.isEmpty() || !headers.contains("X-Forwarded-Proto")) {
            return false;
        }

        var proto = headers.get("X-Forwarded-Proto");
        var host  = headers.get(HttpHeaderNames.HOST);

        return host != null && proto.equalsIgnoreCase("http");
    }

    private void redirectToHttps(WebConnection webConnection) {
        var host       = webConnection.request().headers().get(HttpHeaderNames.HOST);
        var requestUri = webConnection.request().uri();
        var separator  = requestUri.startsWith("/") ? "" : "/";

        webConnection.movedpermanently("https://" + host + separator + requestUri);
    }

    private boolean isUnderMaintenance(WebConnection webConnection) {
        if (!GameConfiguration.getInstance().getBoolean("maintenance")) {
            return false;
        }

        var route = webConnection.getRouteRequest();

        return !route.startsWith("/api")
            && !route.startsWith("/maintenance")
            && !route.startsWith("/" + Routes.HOUSEKEEPING_PATH);
    }

    private void handleAuthenticatedRoute(WebConnection webConnection) {
        if (webConnection.getRouteRequest().equals("/client")) {
            var expiry = DateUtil.getCurrentTimeSeconds() + SessionUtil.REAUTHENTICATE_TIME;
            webConnection.session().set("lastRequest", String.valueOf(expiry));
        }

        var lastRequest  = webConnection.session().getLongOrElse("lastRequest", 0);
        var sessionValid = lastRequest > 0
            && DateUtil.getCurrentTimeSeconds() <= lastRequest;

        webConnection.session().set("clientAuthenticate", !sessionValid);
    }
}
