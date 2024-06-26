package kts.dev.ktsbk.common.utils;


public enum KbErr {
    SUCCESS("ktsbk.kberr.success"),
    USER_BLOCKED("ktsbk.kberr.user_blocked"),
    USER_DISABLED("ktsbk.kberr.user_disabled"),
    USER_NOT_FOUND("ktsbk.kberr.user_not_found"),
    USER_ALREADY_EXISTS("ktsbk.kberr.user_already_exists"),
    ACCOUNT_BLOCKED("ktsbk.kberr.account_blocked"),
    ACCOUNT_DISABLED("ktsbk.kberr.account_disabled"),
    ACCOUNT_NOT_FOUND("ktsbk.kberr.account_not_found"),
    ACCOUNT_ALREADY_EXISTS("ktsbk.kberr.account_already_exists"),
    CURRENCY_BLOCKED("ktsbk.kberr.currency_blocked"),
    CURRENCY_DISABLED("ktsbk.kberr.currency_disabled"),
    CURRENCY_NOT_FOUND("ktsbk.kberr.currency_not_found"),
    CURRENCY_ALREADY_EXISTS("ktsbk.kberr.currency_already_exists"),
    SERVER_BLOCKED("ktsbk.kberr.server_blocked"),
    SERVER_DISABLED("ktsbk.kberr.server_disabled"),
    SERVER_NOT_FOUND("ktsbk.kberr.server_not_found"),
    SERVER_ALREADY_EXISTS("ktsbk.kberr.server_already_exists"),
    SIDE_BUY_BOX_NOT_FOUND("ktsbk.kberr.side_buy_box_not_found"),
    SIDE_BUY_BOX_ALREADY_EXISTS("ktsbk.kberr.side_buy_box_already_exists"),
    COUNT_TRIES_LEFT("ktsbk.kberr.count_tries_left"),
    AUTH_ERROR("ktsbk.kberr.auth_error"),
    PERMISSION_ERROR("ktsbk.kberr.base.permission_error"),
    APPLICATION_EXCEPTION("ktsbk.kberr.application_exception"),
    WORLD_ALREADY_EXISTS("ktsbk.kberr.world_already_exists"),
    PERM_NOT_FOUND("ktsbk.kberr.permission_not_found"),
    WORLD_NOT_FOUND("ktsbk.kberr.world_not_found"),
    BOX_NOT_FOUND("ktsbk.kberr.box_not_found"),
    DIFFERENT_CURRENCIES("ktsbk.kberr.different_currencies"),
    ILLEGAL_COUNT("ktsbk.kberr.illegal_count"),
    NO_MONEY("ktsbk.kberr.no_money"),
    MAX_ACCOUNT_LIMIT("ktsbk.kberr.max_account_limit"),
    MAX_ACCOUNT_LENGTH("ktsbk.kberr.max_account_length"),
    MIN_ACCOUNT_LENGTH("ktsbk.kberr.min_account_length"),
    ILLEGAL_NAME("ktsbk.kberr.illegal_name"),
    ILLEGAL_COST("ktsbk.kberr.illegal_cost"),
    OWNER_NOT_FOUND("ktsbk.kberr.owner_not_found"),
    NOT_IMPLEMENTED("ktsbk.kberr.not_implemented"),
    ACCOUNT_EQUAL_ACCOUNT("ktsbk.kberr.account_eq_account"),
    CONNECTION_ERROR("ktsbk.kberr.connection_error"),
    ERROR_BOX_ACTION_TYPE("ktsbk.kberr.error_box_action_type"),
    BOX_TYPE_NOT_SUPPORT_CURRENCY("ktsbk.kberr.box_type_not_support_currency")
    ;

    private final String translation_key;
    KbErr(String translation_key) {
        this.translation_key = translation_key;
    }
    public String translatable() {
        return this.translation_key;
    }
}
