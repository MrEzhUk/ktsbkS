package kts.dev.ktsbk.common.remote;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public interface RemoteCall {
    UUID getId();
    void regenUUID();
    RemoteType getType();
    String getMethodName();
    Object[] oargs(Method method);
    String json();
}
