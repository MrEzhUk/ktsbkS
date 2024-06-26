package kts.dev.ktsbk.common.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class RemoteBase implements RemoteReturn, RemoteCall, Serializable {
    private final static Gson GSON = new GsonBuilder()
            .create();

    private final static XStream xsteam = new XStream(new JettisonMappedXmlDriver());
    static {
        //xsteam.addPermission(AnyTypePermission.ANY);
        xsteam.allowTypesByRegExp(new String[]{"kts\\.dev\\.ktsbk\\.common\\..+"});
    }

    RemoteType type;
    String methodName;
    String[] args;
    UUID id;

    private RemoteBase(RemoteType type, UUID id, String methodName, String[] args) {
        this.id = id;
        this.type = type;
        this.methodName = methodName;
        this.args = args;
    }
    
    public static RemoteReturn RemoteReturn(UUID id, String methodName, String arg) {
        return new RemoteBase(RemoteType.REMOTE_RETURN, id, methodName, new String[]{arg});
    }
    public static RemoteReturn RemoteReturn(UUID id, Method method, String arg) {
        return RemoteReturn(id, method.getName(), arg);
    }
    public static RemoteReturn RemoteReturn(UUID id, Method method, Object arg) {
        //return RemoteReturn(id, method, GSON.toJson(arg));
        return RemoteReturn(id, method, xsteam.toXML(arg));
    }
    
    public static RemoteCall RemoteCall(String methodName, String... args) {
        return new RemoteBase(RemoteType.REMOTE_CALL, UUID.randomUUID(), methodName, args);
    }
    public static RemoteCall RemoteCall(Method method, String... args) {
        return RemoteCall(method.getName(), args);
    }
    public static RemoteCall RemoteCall(String methodName, Object... args) {
        String[] s = new String[args.length];
        for(int i = 0; i < s.length; i++) {
            //s[i] = GSON.toJson(args[i]);
            s[i] = xsteam.toXML(args[i]);
        }
        return RemoteCall(methodName, s);
    }
    public static RemoteCall RemoteCall(Method method, Object... args) {
        return RemoteCall(method.getName(), args);
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public void regenUUID() {
        this.id = UUID.randomUUID();
    }

    public RemoteType getType() {
        return type;
    }
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String json() {
        return xsteam.toXML(this);
        //return GSON.toJson(this);
    }

    public static RemoteBase json(String msg) throws JsonSyntaxException {
        //return GSON.fromJson(msg, RemoteBase.class);
        return (RemoteBase) xsteam.fromXML(msg);
    }

    @Override
    public Object[] oargs(Method method) {
        Object[] oargs = new Object[this.args.length];
        Class<?>[] cls = method.getParameterTypes();

        for (int i = 0; i < this.args.length; i++) {
            //oargs[i] = GSON.fromJson(this.args[i], cls[i]);
            oargs[i] = xsteam.fromXML(this.args[i]);
        }
        return oargs;
    }

    @Override
    public Object oarg(Method method) {
        return xsteam.fromXML(args[0]);
        //return GSON.fromJson(args[0], method.getReturnType());
    }

    @Override
    public String toString() {
        if(getType() == RemoteType.REMOTE_CALL) {
            return "RemoteCall{" +
                    "methodName='" + methodName + '\'' +
                    ", args=" + (args != null ? Arrays.toString(args) : "(args null)") +
                    '}';
        } else if(getType() == RemoteType.REMOTE_RETURN) {
            return "RemoteReturn{" +
                    "methodName='" + methodName + '\'' +
                    ", arg=" + (args != null ? args[0] : "(args null)") +
                    '}';
        } else {
            return "RemoteUnknown{}";
        }
    }
}
