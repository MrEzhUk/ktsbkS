package kts.dev.ktsbk.common.db.box;

public enum KtsBoxType {
    BUY_ONLY,
    SELL_ONLY,
    SELL_CONFIRM_ONLY,
    BUY_SELL,
    BUY_SELL_CONFIRM
    ;
    final int spec;
    public static final int ALL = (1 << KtsBoxType.values().length) - 1;
    KtsBoxType() {
        this.spec = 1 << this.ordinal();
    }
    public int spec() {
        return spec;
    }
    public boolean isBuy() {
        return (this == BUY_ONLY) || (this == BUY_SELL) || (this == BUY_SELL_CONFIRM);
    }
    public boolean isSell() {
        return (this == SELL_ONLY) || (this == BUY_SELL) || (this == SELL_CONFIRM_ONLY) || (this == BUY_SELL_CONFIRM);
    }
}
