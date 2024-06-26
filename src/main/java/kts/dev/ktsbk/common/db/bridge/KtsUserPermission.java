package kts.dev.ktsbk.common.db.bridge;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.common.db.users.KtsPermission;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.server.KtsDbSource;

import java.io.Serializable;

@DatabaseTable(tableName = "user_permission")
public class KtsUserPermission implements Serializable {
    public static final Dao<KtsUserPermission, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsUserPermission.class);
    public KtsUserPermission() {}
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    @DatabaseField(columnName = "user_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsUser user;

    @DatabaseField(columnName = "permission_id", foreign=true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private KtsPermission permission;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public KtsUser getUser() {
        return user;
    }

    public void setUser(KtsUser user) {
        this.user = user;
    }

    public KtsPermission getPermission() {
        return permission;
    }

    public void setPermission(KtsPermission permission) {
        this.permission = permission;
    }
}
