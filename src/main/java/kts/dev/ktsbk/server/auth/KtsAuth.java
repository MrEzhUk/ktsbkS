package kts.dev.ktsbk.server.auth;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.common.db.users.KtsUser;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.net.InetAddress;

@DatabaseTable(tableName = "auth")
public class KtsAuth implements Serializable {
    public static final Dao<KtsAuth, Long> DAO = KtsDbSource.INSTANCE.createDao(KtsAuth.class);
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;
    @DatabaseField(columnName = "auth_date_time")
    private DateTime authDateTime = DateTime.now();
    @DatabaseField(columnName = "user_id", foreign=true, foreignAutoCreate = true, foreignAutoRefresh = true)
    KtsUser user;
    @DatabaseField(columnName = "ip", dataType = DataType.SERIALIZABLE)
    private InetAddress ip;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DateTime getAuthDateTime() {
        return authDateTime;
    }

    public void setAuthDateTime(DateTime authDateTime) {
        this.authDateTime = authDateTime;
    }

    public KtsUser getUser() {
        return user;
    }

    public void setUser(KtsUser user) {
        this.user = user;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
}
