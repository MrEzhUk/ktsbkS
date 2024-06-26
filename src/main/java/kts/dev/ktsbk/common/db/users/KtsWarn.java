package kts.dev.ktsbk.common.db.users;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;

import java.sql.Timestamp;
import java.time.Instant;

@DatabaseTable(tableName = "warns")
public class KtsWarn {
    public static final Dao<KtsWarn, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsWarn.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "created_time", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp createdTime = Timestamp.from(Instant.now());
    @DatabaseField(columnName = "before", dataType = DataType.TIME_STAMP_STRING)
    private Timestamp beforeTime;
    @DatabaseField(columnName = "doer", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsUser doer;
    @DatabaseField(columnName = "guilty", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsUser guilty;
    @DatabaseField(columnName = "banned")
    private boolean banned;
    @DatabaseField(columnName = "fine")
    private long fine;
    @DatabaseField(columnName = "punishment_points")
    private long punishmentPoints;
    @DatabaseField(columnName = "message")
    private String message;

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

    public KtsUser getGuilty() {
        return guilty;
    }

    public void setGuilty(KtsUser guilty) {
        this.guilty = guilty;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public long getFine() {
        return fine;
    }

    public void setFine(long fine) {
        this.fine = fine;
    }

    public long getPunishmentPoints() {
        return punishmentPoints;
    }

    public void setPunishmentPoints(long punishmentPoints) {
        this.punishmentPoints = punishmentPoints;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public Timestamp getBeforeTime() {
        return beforeTime;
    }

    public void setBeforeTime(Timestamp beforeTime) {
        this.beforeTime = beforeTime;
    }
}
