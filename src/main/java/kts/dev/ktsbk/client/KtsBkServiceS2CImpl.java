package kts.dev.ktsbk.client;

import kts.dev.ktsbk.common.services.KtsBkServiceS2C;

public class KtsBkServiceS2CImpl implements KtsBkServiceS2C {
    // void chat(String message);
    public void error(String message) {
        System.out.println("Error from server: " + message);
    }
    // void errorKbErr(KbErr kb);
    // void updateAccount(KtsAccount acc);
    // void updateUser(KtsUser usr);
    // void updateBox(KtsBSideBox box);
}
