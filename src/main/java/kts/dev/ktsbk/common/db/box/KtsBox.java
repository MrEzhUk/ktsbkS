package kts.dev.ktsbk.common.db.box;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.multiworld.KtsWorld;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "box")
public class KtsBox implements Serializable {
    public static final Dao<KtsBox, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsBox.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "last_tax_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp lastTaxTime;
    @DatabaseField(columnName = "x", dataType = DataType.LONG)
    private long x;
    @DatabaseField(columnName = "y", dataType = DataType.LONG)
    private long y;
    @DatabaseField(columnName = "z", dataType = DataType.LONG)
    private long z;
    @DatabaseField(columnName = "world", foreign = true, foreignAutoRefresh = true)
    private KtsWorld world;
    @DatabaseField(columnName = "account_id", foreign = true, foreignAutoRefresh = true)
    private KtsAccount account;
    @DatabaseField(columnName = "mc_identifier", dataType = DataType.STRING)
    private String minecraftIdentifier;
    @DatabaseField(columnName = "mc_serialized_item", dataType = DataType.STRING)
    private String minecraftSerializedItem;
    @DatabaseField(columnName = "box_type", dataType = DataType.ENUM_INTEGER)
    private KtsBoxType boxType;
    @DatabaseField(columnName = "count_now", dataType = DataType.LONG)
    private long countNow;
    @DatabaseField(columnName = "count_per_transaction", dataType = DataType.LONG)
    private long countPerTransaction;
    @DatabaseField(columnName = "buy_cost_per_transaction", dataType = DataType.LONG)
    private long buyCostPerTransaction;
    @DatabaseField(columnName = "sell_cost_per_transaction", dataType = DataType.LONG)
    private long sellCostPerTransaction;
    @DatabaseField(columnName = "blocked")
    private boolean blocked = true;
    @DatabaseField(columnName = "disabled")
    private boolean disabled = false;

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

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public long getZ() {
        return z;
    }

    public void setZ(long z) {
        this.z = z;
    }

    public KtsWorld getWorld() {
        return world;
    }

    public void setWorld(KtsWorld world) {
        this.world = world;
    }

    public KtsAccount getAccount() {
        return account;
    }

    public void setAccount(KtsAccount account) {
        this.account = account;
    }

    public String getMinecraftIdentifier() {
        return minecraftIdentifier;
    }

    public void setMinecraftIdentifier(String minecraftIdentifier) {
        this.minecraftIdentifier = minecraftIdentifier;
    }

    public String getMinecraftSerializedItem() {
        return minecraftSerializedItem;
    }

    public void setMinecraftSerializedItem(String minecraftSerializedItem) {
        this.minecraftSerializedItem = minecraftSerializedItem;
    }

    public long getCountPerTransaction() {
        return countPerTransaction;
    }
    public void setCountPerTransaction(long countPerTransaction) {
        this.countPerTransaction = countPerTransaction;
    }
    public long getCountNow() {
        return countNow;
    }
    public void setCountNow(long countNow) {
        this.countNow = countNow;
    }
    public long getBuyCostPerTransaction() {
        return buyCostPerTransaction;
    }
    public void setBuyCostPerTransaction(long buyCostPerTransaction) {
        this.buyCostPerTransaction = buyCostPerTransaction;
    }
    public long getSellCostPerTransaction() {
        return sellCostPerTransaction;
    }
    public void setSellCostPerTransaction(long sellCostPerTransaction) {
        this.sellCostPerTransaction = sellCostPerTransaction;
    }
    public KtsBoxType getBoxType() {
        return boxType;
    }
    public void setBoxType(KtsBoxType boxType) {
        this.boxType = boxType;
    }


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Timestamp getLastTaxTime() {
        return lastTaxTime;
    }

    public void setLastTaxTime(Timestamp lastTaxTime) {
        this.lastTaxTime = lastTaxTime;
    }
}
