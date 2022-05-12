package de.kosmos_lab.platform.plugins.web.chat.websocket;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.WebSocketService;

import io.netty.util.internal.ConcurrentSet;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Extension
@ServerEndpoint("/chatws")
public class ChatWebSocketService extends WebSocketService implements ExtensionPoint {
    private final WebServer server;
    private final IController controller;
    ConcurrentSet<Session> sessions = new ConcurrentSet<>();
    ConcurrentHashMap<Session, String> nicks = new ConcurrentHashMap<>();
    Random rnd = new Random();

    public ChatWebSocketService(WebServer server, IController controller) {
        this.server = server;
        this.controller = controller;



    }

    public String getRandomNick() {

        while (true) {
            int number = rnd.nextInt(999999);

            // this will convert any number sequence into 6 character.
            String n = String.format("Guest-%06d", number);
            if (!nicks.containsKey(n)) {
                return n;
            }
        }
    }

    @Override
    public void addWebSocketClient(Session session) {
        sessions.add(session);
        String n = getRandomNick();
        nicks.put(session, n);
        broadCast(String.format("INFO: %s joined the chat", n));
    }

    @Override
    public void delWebSocketClient(Session session) {
        sessions.remove(session);
    }

    public void broadCastAllButOwn(String message, Session ownSession) {
        for (Session session : this.sessions) {

            try {
                if (session != ownSession) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (org.eclipse.jetty.io.EofException ex) {
                //Nothing here
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isValidNick(String s) {
        if (s == null) {
            return false;
        }
        for (String bad : new String[]{"INFO", "ERROR", "SYSTEM", "CHAT"}) {
            if (s.equals(bad)) {
                return false;
            }
        }
        if (s.matches("\\A[A-Za-z_\\-\\[\\]\\\\^{}|`][A-Za-z0-9_\\-\\[\\]\\\\^{}|`]{2,15}\\z")) {
            return true;
        }
        return false;
    }

    public void broadCast(String message) {
        for (Session session : this.sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (org.eclipse.jetty.io.EofException ex) {
                //Nothing here
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    Pattern nickPattern = Pattern.compile("^/nick (.*)$");

    @Override
    public void onWebSocketMessage(Session session, String message) {
        if (message.startsWith("/")) {
            Matcher m = nickPattern.matcher(message);
            if (m.matches()) {
                if (isValidNick(m.group(1))) {
                    String me = nicks.get(session);
                    nicks.put(session, m.group(1));
                    broadCast(String.format("INFO: %s is now known as %s", me, m.group(1)));

                } else {
                    try {
                        session.getBasicRemote().sendText("ERROR: that nickname is invalid!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (message.equals("/nick")) {
                try {
                    session.getBasicRemote().sendText(String.format("INFO: your nick is %s", nicks.get(session)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.broadCast(String.format("CHAT: %s: %s", this.nicks.get(session), message));
        }
    }
}
