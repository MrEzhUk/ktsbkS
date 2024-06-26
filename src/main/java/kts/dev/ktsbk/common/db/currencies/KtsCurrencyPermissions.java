package kts.dev.ktsbk.common.db.currencies;

import kts.dev.ktsbk.common.db.box.KtsBoxType;

public enum KtsCurrencyPermissions {
    PERM_CREATE_ACCOUNT,
    PERM_PAY_ACCOUNT,
    PERM_INVITE_ACCOUNT,
    PERM_KICK_ACCOUNT,
    PERM_CREATE_BOX
    ;
    final int spec;
    public static final int ALL = (1 << KtsBoxType.values().length) - 1;
    KtsCurrencyPermissions() {
        this.spec = 1 << this.ordinal();
    }
    public int spec() {
        return spec;
    }
}
