package kts.dev.ktsbk.common.db.multiworld;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;

import java.io.Serializable;

@DatabaseTable(tableName = "worlds")
public class KtsWorld implements Serializable {
    public static final Dao<KtsWorld, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsWorld.class);
    @DatabaseField(generatedId = true, columnName = "id")
    private long id;
    @DatabaseField(columnName = "server_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsServer server;
    @DatabaseField(columnName = "minecraft_name")
    private String mcName;
    @DatabaseField(columnName = "ktsbk_name")
    private String ktsbkName;
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

    public String getMcName() {
        return mcName;
    }

    public void setMcName(String mcName) {
        this.mcName = mcName;
    }

    public String getKtsbkName() {
        return ktsbkName;
    }

    public void setKtsbkName(String ktsbkName) {
        this.ktsbkName = ktsbkName;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
