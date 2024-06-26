package kts.dev.ktsbk.server.dao.users;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import kts.dev.ktsbk.common.db.users.KtsUser;

import java.sql.SQLException;

public class KtsUserDaoImpl extends BaseDaoImpl<KtsUser, Long> implements KtsUserDao {
    public KtsUserDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, KtsUser.class);
    }


}
