package kts.dev.ktsbk.common.remote;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public interface RemoteReturn {
    UUID getId();
    RemoteType getType();
    String getMethodName();
    String json();
    Object oarg(Method method);
}
