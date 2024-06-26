package kts.dev.ktsbk.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.remote.RemoteBase;
import kts.dev.ktsbk.common.remote.RemoteCall;
import kts.dev.ktsbk.common.remote.RemoteReturn;
import kts.dev.ktsbk.common.remote.RemoteType;
import kts.dev.ktsbk.common.services.KtsBkServiceS2C;
import kts.dev.ktsbk.common.utils.AuthContext;
import kts.dev.ktsbk.common.utils.Pair;
import kts.dev.ktsbk.server.auth.AuthManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KtsBkServer extends WebSocketServer {
    private final Map<String, Method> methods = new HashMap<>();
    private final Object realizationC2S;
    Map<WebSocket, KtsUser> authMap;
    public KtsBkServer(InetSocketAddress address, Object realizationC2S) {
        super(address);
        this.setReuseAddr(true);
        this.setConnectionLostTimeout(3);
        this.realizationC2S = realizationC2S;
        for(Method method: realizationC2S.getClass().getDeclaredMethods()) {
            this.methods.put(method.getName(), method);
        }
        authMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String token = handshake.getFieldValue("token");
        if(token == null || token.isEmpty()) {
            conn.send(RemoteBase.RemoteCall("error", (Object) "token not found.").json());
            conn.close();
            return;
        }
        KtsUser usr = AuthManager.INSTANCE.retrieveUser(token);
        if(usr == null) {
            conn.send(RemoteBase.RemoteCall("error", (Object) "incorrect token.").json());
            conn.close();
            return;
        }
        if(usr.isBlocked()) {
            conn.send(RemoteBase.RemoteCall("error", (Object) "you blocked in system, please write to administration.").json());
            conn.close();
            return;
        }
        authMap.put(conn, usr);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        authMap.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        //System.out.println(message);
        RemoteBase rb;
        try {
            rb = RemoteBase.json(message);
        } catch (RuntimeException e) {
            return;
        }

        Method method = this.methods.get(rb.getMethodName());
        if(method == null) {
            send(conn, RemoteBase.RemoteReturn(rb.getId(), rb.getMethodName(), null));
            conn.send(RemoteBase.RemoteCall("error", (Object) "incorrect method.").json());
            conn.close();
            return;
        }

        try {
            //System.out.println(rb);
            if(rb.getType() == RemoteType.REMOTE_CALL) {
                Object[] oargs = rb.oargs(method);
                //System.out.println(Arrays.toString(oargs));
                if(oargs.length >= 1) {
                    AuthContext auth = new AuthContext();
                    KtsUser usr = authMap.get(conn);
                    if(usr == null) {
                        conn.send(RemoteBase.RemoteCall("error", (Object) "incorrect auth.").json());
                        send(conn, RemoteBase.RemoteReturn(rb.getId(), method, null));
                        conn.close();
                        //conn.closeConnection(403, "incorrect auth.");
                        return;
                    }
                    auth.setWs(conn);
                    auth.setUser(usr);
                    oargs[0] = auth;
                }
                Object o = method.invoke(realizationC2S, oargs);
                //System.out.println(o);
                send(conn, RemoteBase.RemoteReturn(rb.getId(), method, o));
            } else if(rb.getType() == RemoteType.REMOTE_RETURN) {
                //System.out.println("AAAAAAA Remote Return");
                //conn.send(RemoteBase.RemoteCall("error", (Object) "remote_return on server side not support.").json());
                //conn.close();
                //conn.closeConnection(403, "remote_return on server side not support.");
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | JsonSyntaxException e) {
            //throw  new RuntimeException(e);
            send(conn, RemoteBase.RemoteReturn(rb.getId(), method, null));
            send(conn, RemoteBase.RemoteCall("error", (Object) "internal application error."));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        conn.send(RemoteBase.RemoteCall("error", (Object) ex.toString()).json());
    }

    @Override
    public void onStart() {

    }

    public void send(WebSocket w, RemoteReturn rr) {
        w.send(rr.json());
    }
    public void send(WebSocket w, RemoteCall rc) {
        w.send(rc.json());
    }
}
