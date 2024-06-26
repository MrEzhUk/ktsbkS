package kts.dev.ktsbk.server.dao.bridge;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;
import kts.dev.ktsbk.server.dao.accounts.KtsAccountDao;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;

import java.sql.SQLException;
import java.util.List;

public class KtsUserAccountDaoImpl extends BaseDaoImpl<KtsUserAccount, Long> implements KtsUserAccountDao {
    private final KtsUserDao usrDao;
    private final KtsAccountDao accDao;
    public KtsUserAccountDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, KtsUserAccount.class);
        usrDao = DaoManager.createDao(connectionSource, KtsUser.class);
        accDao = DaoManager.createDao(connectionSource, KtsAccount.class);
    }

    @Override
    public List<KtsAccount> getMemberShip(KtsUser usr) throws SQLException {
        QueryBuilder<KtsUserAccount, Long> q = this.queryBuilder();
        q.where().eq("user_id", usr.getId());
        return accDao.queryBuilder().join(q).query();
    }

    @Override
    public boolean isMemberShip(KtsUser usr, KtsAccount account) {
        try {
            return this.queryBuilder().where()
                    .eq("account_id", account.getId())
                    .and()
                    .eq("user_id", usr.getId())
                    .queryForFirst() != null;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<KtsUser> getMemberShip(KtsAccount acc) throws SQLException {
        QueryBuilder<KtsUserAccount, Long> q = this.queryBuilder();
        q.where().eq("account_id", acc.getId());
        return usrDao.queryBuilder().join(q).query();
    }

    public boolean isMemberShip(KtsAccount acc, KtsUser user) {
        try {
            return this.queryBuilder().where()
                    .eq("account_id", acc.getId())
                    .and()
                    .eq("user_id", user.getId())
                    .queryForFirst() != null;
        } catch (SQLException e) {
            return false;
        }
    }
}
