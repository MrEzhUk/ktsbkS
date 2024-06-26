package kts.dev.ktsbk.common.db.users;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;

import java.io.Serializable;

@DatabaseTable(tableName = "permissions")
public class KtsPermission implements Serializable {
    public static final Dao<KtsPermission, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsPermission.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "permission_string")
    private String permissionString;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }
}
