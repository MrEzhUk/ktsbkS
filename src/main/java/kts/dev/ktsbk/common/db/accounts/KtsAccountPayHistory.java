package kts.dev.ktsbk.common.db.accounts;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.common.db.users.KtsUser;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@DatabaseTable(tableName = "account_pay_history")
public class KtsAccountPayHistory implements Serializable {
    public static final Dao<KtsAccountPayHistory, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsAccountPayHistory.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "doer", foreign = true)
    KtsUser doer;
    @DatabaseField(columnName = "from_account", foreign = true, foreignAutoRefresh = true)
    KtsAccount fromAccount;
    @DatabaseField(columnName = "to_account", foreign = true, foreignAutoRefresh = true)
    KtsAccount toAccount;
    @DatabaseField(columnName = "count")
    long count;
    @DatabaseField(columnName = "msg")
    String msg;
    @DatabaseField(columnName = "rollback")
    boolean rollback = false;

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

    public KtsAccount getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(KtsAccount fromAccount) {
        this.fromAccount = fromAccount;
    }

    public KtsAccount getToAccount() {
        return toAccount;
    }

    public void setToAccount(KtsAccount toAccount) {
        this.toAccount = toAccount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
