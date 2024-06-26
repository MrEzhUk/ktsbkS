package kts.dev.ktsbk.server;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import kts.dev.ktsbk.common.db.accounts.*;
import kts.dev.ktsbk.common.db.box.KtsBoxTax;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.currencies.KtsCurrency;
import kts.dev.ktsbk.common.db.multiworld.KtsServer;
import kts.dev.ktsbk.common.db.multiworld.KtsWorld;
import kts.dev.ktsbk.common.db.users.KtsPermission;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;
import kts.dev.ktsbk.common.db.bridge.KtsUserPermission;
import kts.dev.ktsbk.common.db.users.KtsWarn;
import kts.dev.ktsbk.server.balance.KtsOptimizedBalance;

import java.sql.SQLException;

public class KtsDbSource {
    public static final KtsDbSource INSTANCE = new KtsDbSource();
    private final JdbcPooledConnectionSource connectionSource;
    protected KtsDbSource() {
        try {
            connectionSource = new JdbcPooledConnectionSource("jdbc:db:url", "user", "password");
            TableUtils.createTableIfNotExists(connectionSource, KtsUser.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsPermission.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsUserPermission.class);

            TableUtils.createTableIfNotExists(connectionSource, KtsServer.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsWorld.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsCurrency.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsAccount.class);

            TableUtils.createTableIfNotExists(connectionSource, KtsUserAccount.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsAccountPayHistory.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsBox.class);

            TableUtils.createTableIfNotExists(connectionSource, KtsBoxTax.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsAccountBuyHistory.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsAccountSellHistory.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsOptimizedBalance.class);
            TableUtils.createTableIfNotExists(connectionSource, KtsAccountIOMoneyHistory.class);

            TableUtils.createTableIfNotExists(connectionSource, KtsWarn.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public JdbcPooledConnectionSource getSource() {
        return connectionSource;
    }

    public synchronized <D extends Dao<T, ?>, T> D createDao(Class<T> daoClass) {
        try {
            return DaoManager.createDao(connectionSource, daoClass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
