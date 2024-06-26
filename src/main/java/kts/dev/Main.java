package kts.dev;

import kts.dev.ktsbk.server.KtsBkServer;
import kts.dev.ktsbk.server.KtsBkServiceC2SImpl;
import java.net.InetSocketAddress;

public class Main {

    public static final KtsBkServiceC2SImpl service = new KtsBkServiceC2SImpl();

    public static void main(String[] args) throws Exception {
        int port = 8001;
        KtsBkServer s = new KtsBkServer(new InetSocketAddress(port), service);
        s.start();
    }
}