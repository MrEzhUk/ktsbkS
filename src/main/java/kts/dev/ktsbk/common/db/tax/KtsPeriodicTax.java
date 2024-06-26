package kts.dev.ktsbk.common.db.tax;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;

import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "periodic_tax")
public class KtsPeriodicTax {
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "last_tax_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp lastTaxTime;
    @DatabaseField(columnName = "tax_payer_account_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount taxPayerAccount;
    @DatabaseField(columnName = "tax_recipient_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount taxRecipientAccount;
    @DatabaseField(columnName = "tax_count", dataType = DataType.LONG)
    private long taxCount;
    @DatabaseField(columnName = "day_count", dataType = DataType.LONG)
    private long dayCount;
    //@DatabaseField(columnName = )
    @DatabaseField(columnName = "blocked")
    private boolean blocked = true;
    @DatabaseField(columnName = "disabled")
    private boolean disabled = false;
}
