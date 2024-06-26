package kts.dev.ktsbk.client;

import com.google.gson.JsonSyntaxException;
import kts.dev.ktsbk.common.remote.RemoteBase;
import kts.dev.ktsbk.common.remote.RemoteCall;
import kts.dev.ktsbk.common.remote.RemoteReturn;
import kts.dev.ktsbk.common.remote.RemoteType;
import kts.dev.ktsbk.common.services.KtsBkServiceC2S;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KtsBkWsClient extends WebSocketClient implements InvocationHandler {
    public final KtsBkServiceC2S service = (KtsBkServiceC2S) Proxy.newProxyInstance(
            KtsBkServiceC2S.class.getClassLoader(),
            new Class[] {KtsBkServiceC2S.class},
            this
    );
    private final Map<UUID, RemoteReturn> resp = new ConcurrentHashMap<>();
    private final Map<String, Method> methods = new HashMap<>();
    private final Object realizationS2C;
    public KtsBkWsClient(URI uri, Object realizationS2C) {
        super(uri);
        this.realizationS2C = realizationS2C;
        for(Method method: realizationS2C.getClass().getDeclaredMethods()) {
            this.methods.put(method.getName(), method);
        }
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
        try {
            RemoteBase rb = RemoteBase.json(message);
            if (rb.getType() == RemoteType.REMOTE_CALL) {
                Method method = this.methods.get(rb.getMethodName());
                Object o = method.invoke(realizationS2C, rb.oargs(method));
                send(RemoteBase.RemoteReturn(rb.getId(), method, o));
            } else if (rb.getType() == RemoteType.REMOTE_RETURN) {
                resp.put(rb.getId(), rb);
                synchronized (resp) {
                    resp.notifyAll();
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | JsonSyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        synchronized (this.resp) {
            this.resp.notifyAll();
        }
    }

    @Override
    public void onError(Exception ex) {

    }
    public void send(RemoteCall call) {
        this.send(call.json());
    }
    public void send(RemoteReturn ret) {
        this.send(ret.json());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(this.isClosed()) this.reconnectBlocking(); // важен именно этот порядок в противном случае бесконечный reconnect
        if(this.getReadyState() == ReadyState.NOT_YET_CONNECTED) this.connectBlocking();

        RemoteCall call = RemoteBase.RemoteCall(method, args);

        this.send(call);
        if(method.getReturnType() == void.class || method.getReturnType() == Void.class) return null;

        synchronized (this.resp) {
            while(true) {
                this.resp.wait();
                if(this.isClosed()) break;
                RemoteReturn rr = this.resp.remove(call.getId());
                if(rr != null) return rr.oarg(method);
            }
        }
        throw new ConnectException("Socket closed");
    }
}
