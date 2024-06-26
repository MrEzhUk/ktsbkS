package kts.dev.ktsbk.server.dao.bridge;

import com.j256.ormlite.dao.Dao;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;

import java.sql.SQLException;
import java.util.List;

public interface KtsUserAccountDao extends Dao<KtsUserAccount, Long> {
    List<KtsAccount> getMemberShip(KtsUser usr) throws SQLException;
    boolean isMemberShip(KtsUser usr, KtsAccount account);
    List<KtsUser> getMemberShip(KtsAccount acc) throws SQLException;
    boolean isMemberShip(KtsAccount acc, KtsUser user);
}
