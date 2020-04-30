package ru.geek.news_portal.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * GeekBrains Java, news_portal.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 30.04.2020
 * @link https://github.com/Centnerman
 */

public class HttpRequestUtils {
    public static URL getAppUrl(HttpServletRequest req)
            throws MalformedURLException {
        String scheme = req.getScheme();
        String host = req.getServerName();
        int port = req.getServerPort();
        String contextPath = req.getContextPath();

        return new URL(scheme, host, port, contextPath);
    }
}
