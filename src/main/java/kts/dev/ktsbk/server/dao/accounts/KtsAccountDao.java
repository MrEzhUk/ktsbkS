package kts.dev.ktsbk.server.dao.accounts;


import com.j256.ormlite.dao.Dao;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;

import java.sql.SQLException;
import java.util.List;

public interface KtsAccountDao extends Dao<KtsAccount, Long> {

}
