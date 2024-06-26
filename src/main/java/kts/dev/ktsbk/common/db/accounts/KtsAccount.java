package kts.dev.ktsbk.common.db.accounts;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.common.db.currencies.KtsCurrency;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.server.balance.BalanceManager;
import kts.dev.ktsbk.server.dao.accounts.KtsAccountDaoImpl;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/*
 * Примечание на стороне криента не должно быть _balance и sendBalance.
 */
@DatabaseTable(tableName = "accounts", daoClass = KtsAccountDaoImpl.class)
public class KtsAccount implements Serializable {
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "user_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsUser user;
    @DatabaseField(columnName = "name")
    private String name;
    @DatabaseField(columnName = "currency_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsCurrency currency;
    private long balance = 0L;
    @DatabaseField(columnName = "blocked")
    boolean blocked = false;
    @DatabaseField(columnName = "disabled")
    boolean disabled = false;

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

    public KtsUser getUser() {
        return user;
    }

    public void setUser(KtsUser user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KtsCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KtsCurrency currency) {
        this.currency = currency;
    }
    public void loadBalance() throws SQLException {
        this.balance = BalanceManager.INSTANCE.getBalance(this);
    }
    public long getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }
    public boolean climbBlocked() {
        return isBlocked() || currency.isBlocked();
    }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    public boolean isDisabled() {
        return disabled;
    }
    public boolean climbDisabled() {
        return isDisabled() || currency.isDisabled();
    }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
