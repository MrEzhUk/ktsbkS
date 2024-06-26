package kts.dev.ktsbk.server.dao.accounts;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;

import java.sql.SQLException;

public class KtsAccountDaoImpl extends BaseDaoImpl<KtsAccount, Long> implements KtsAccountDao {
    public KtsAccountDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, KtsAccount.class);
    }
}
