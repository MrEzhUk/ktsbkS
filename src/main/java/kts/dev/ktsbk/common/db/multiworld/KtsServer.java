package kts.dev.ktsbk.common.db.multiworld;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;

import java.io.Serializable;

@DatabaseTable(tableName = "servers")
public class KtsServer implements Serializable {
    public static final Dao<KtsServer, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsServer.class);
    @DatabaseField(generatedId = true, columnName = "id")
    private long id;
    @DatabaseField(columnName = "name")
    private String name;
    @DatabaseField(columnName = "short_name")
    private String shortName;
    @DatabaseField(columnName = "ip")
    private String ip;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
}
