package kts.dev.ktsbk.common.db.accounts;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.server.KtsDbSource;

import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "account_sell_history")
public class KtsAccountSellHistory {
    public static final Dao<KtsAccountSellHistory, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsAccountSellHistory.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "doer", foreign = true)
    KtsUser doer;
    @DatabaseField(columnName = "account_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount account;
    @DatabaseField(columnName = "box_id", foreign = true, foreignAutoRefresh = true)
    private KtsBox box;
    @DatabaseField(columnName = "count")
    private long count;
    @DatabaseField(columnName = "cost")
    private long cost;
    @DatabaseField(columnName = "tax")
    private long tax;
    @DatabaseField(columnName = "rollback")
    private boolean rollback = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public KtsUser getDoer() {
        return doer;
    }

    public void setDoer(KtsUser doer) {
        this.doer = doer;
    }

    public KtsAccount getAccount() {
        return account;
    }

    public void setAccount(KtsAccount account) {
        this.account = account;
    }

    public KtsBox getBox() {
        return box;
    }

    public void setBox(KtsBox box) {
        this.box = box;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }
}
