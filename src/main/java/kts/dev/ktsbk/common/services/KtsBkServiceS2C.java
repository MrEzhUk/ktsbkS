package kts.dev.ktsbk.common.services;

import org.java_websocket.WebSocket;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface KtsBkServiceS2C {
    // void chat(KtsBkContextS2C ctx, String message);
    // void updateRequest(KtsBkContextS2C ctx, String message);

    // List<String> getPlayerList(KtsBkContextS2C ctx);

    // todo: function to update accounts/boxes/other in real time.
    void error(String message);
}
