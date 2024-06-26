package kts.dev.ktsbk.server.balance;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "optimized_balance")
public class KtsOptimizedBalance {
    public static final Dao<KtsOptimizedBalance, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsOptimizedBalance.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "last_change_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp lastChangedTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "account_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount account;
    @DatabaseField(columnName = "balance")
    private long balance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getLastChangedTime() {
        return lastChangedTime;
    }

    public void setLastChangedTime(Timestamp lastChangedTime) {
        this.lastChangedTime = lastChangedTime;
    }

    public KtsAccount getAccount() {
        return account;
    }

    public void setAccount(KtsAccount account) {
        this.account = account;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
