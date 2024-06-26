package kts.dev.ktsbk.common.db.box;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.server.dao.box.KtsBoxTaxDaoImpl;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "account_box_tax", daoClass = KtsBoxTaxDaoImpl.class)
public class KtsBoxTax {
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "doer", foreign = true)
    KtsUser doer;
    @DatabaseField(columnName = "account_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount account;
    @DatabaseField(columnName = "count")
    private long count;
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

    public KtsAccount getAccount() {
        return account;
    }

    public void setAccount(KtsAccount account) {
        this.account = account;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public KtsUser getDoer() {
        return doer;
    }

    public void setDoer(KtsUser doer) {
        this.doer = doer;
    }

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }
}
