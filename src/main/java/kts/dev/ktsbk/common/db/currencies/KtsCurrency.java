package kts.dev.ktsbk.common.db.currencies;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.common.db.multiworld.KtsServer;

import java.io.Serializable;

@DatabaseTable(tableName = "currencies")
public class KtsCurrency implements Serializable {
    public static final Dao<KtsCurrency, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsCurrency.class);
    @DatabaseField(generatedId = true, columnName = "id")
    private long id;
    @DatabaseField(columnName = "server_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsServer server;
    @DatabaseField(columnName = "name")
    private String name;
    @DatabaseField(columnName = "short_name")
    private String shortName;
    @DatabaseField(columnName = "box_rent")
    private long boxRent = 0;
    @DatabaseField(columnName = "transaction_percent")
    private long transactionPercent = 0;
    @DatabaseField(columnName = "max_accounts")
    private long maxAccounts = 16;
    @DatabaseField(columnName = "box_type_support")
    private int boxTypeSupport = 0;
    @DatabaseField(columnName = "currency_permission")
    private int currencyPermission = 0;
    @DatabaseField(columnName = "day_3_acquire")
    private int day3Acquire = 0;
    @DatabaseField(columnName = "blocked")
    private boolean blocked = false;
    @DatabaseField(columnName = "disabled")
    private boolean disabled = false;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public KtsServer getServer() {
        return server;
    }

    public void setServer(KtsServer serverId) {
        this.server = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public long getBoxRent() {
        return boxRent;
    }

    public void setBoxRent(long boxRent) {
        this.boxRent = boxRent;
    }

    public long getTransactionPercent() {
        return transactionPercent;
    }

    public void setTransactionPercent(long transactionPercent) {
        this.transactionPercent = transactionPercent;
    }

    public long getMaxAccounts() {
        return maxAccounts;
    }

    public void setMaxAccounts(long maxAccounts) {
        this.maxAccounts = maxAccounts;
    }

    public boolean isBlocked() {
        return blocked;
    }
    public boolean climbBlocked() {
        return isBlocked() || server.isBlocked();
    }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDisabled() {
        return disabled;
    }
    public boolean climbDisabled() {
        return isDisabled() || server.isDisabled();
    }

    public int getCurrencyPermission() {
        return currencyPermission;
    }

    public void setCurrencyPermission(int currencyPermission) {
        this.currencyPermission = currencyPermission;
    }

    public int getDay3Acquire() {
        return day3Acquire;
    }

    public void setDay3Acquire(int day3Acquire) {
        this.day3Acquire = day3Acquire;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    public int getBoxTypeSupport() {
        return boxTypeSupport;
    }

    public void setBoxTypeSupport(int boxTypeSupport) {
        this.boxTypeSupport = boxTypeSupport;
    }
}
