package kts.dev.ktsbk.server.balance;

import com.j256.ormlite.stmt.QueryBuilder;
import kts.dev.ktsbk.common.db.accounts.*;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.box.KtsBoxTax;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.dao.box.KtsBoxTaxDao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class BalanceManager {
    public static final BalanceManager INSTANCE = new BalanceManager();
    private static final KtsBoxTaxDao boxTaxDao = KtsDbSource.INSTANCE.createDao(KtsBoxTax.class);
    private BalanceManager() {}
    public void resetCachedPassword(List<Long> account_ids) throws SQLException {
        for(long id : account_ids) {
            KtsOptimizedBalance opt_balance = KtsOptimizedBalance.DAO.queryForId(id);
            if(opt_balance != null) KtsOptimizedBalance.DAO.delete(opt_balance);
        }
    }
    public long getBalance(KtsAccount account) throws SQLException {
        List<KtsOptimizedBalance> opts = KtsOptimizedBalance.DAO.queryForEq("account_id", account.getId());
        KtsOptimizedBalance opt;
        if(!opts.isEmpty()) {
            opt = opts.get(0);
        } else {
            opt = new KtsOptimizedBalance();
            opt.setBalance(0);
            opt.setLastChangedTime(new Timestamp(0));
            opt.setAccount(account);
        }

        long balance = 0;
        Timestamp fixedTime = Timestamp.from(Instant.now());

        // pay transactions
        List<KtsAccountPayHistory> history = KtsAccountPayHistory.DAO.queryBuilder().where()
                .eq("from_account", account.getId()).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();
        for(var t : history) {
            balance -= t.getCount();
        }
        history = KtsAccountPayHistory.DAO.queryBuilder().where()
                .eq("to_account", account.getId()).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();
        for(var t : history) {
            balance += t.getCount();
        }

        // buy transactions
        List<KtsAccountBuyHistory> buyHistory = KtsAccountBuyHistory.DAO.queryBuilder().where()
                .eq("account_id", account.getId()).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();

        for(var t : buyHistory) {
            balance -= t.getCount() * t.getCost() + t.getTax();
        }

        QueryBuilder<KtsBox, Long> q = KtsBox.DAO.queryBuilder();
        q.where().eq("account_id", account.getId());
        buyHistory = KtsAccountBuyHistory.DAO.queryBuilder().join(q).where()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();

        for(var t : buyHistory) {
            balance += t.getCount() * t.getCost();
        }

        // sell transactions
        List<KtsAccountSellHistory> sellHistory;
        sellHistory = KtsAccountSellHistory.DAO.queryBuilder().where()
                .eq("account_id", account.getId()).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();

        for(var t : sellHistory) {
            balance += t.getCount() * t.getCost();
        }

        q = KtsBox.DAO.queryBuilder();
        q.where().eq("account_id", account.getId());
        sellHistory = KtsAccountSellHistory.DAO.queryBuilder().join(q).where()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();

        for(var t : sellHistory) {
            balance -= t.getCount() * t.getCost() + t.getTax();
        }

        // money_io transactions
        List<KtsAccountIOMoneyHistory> ioMoneyHistory = KtsAccountIOMoneyHistory.DAO.queryBuilder().where()
                .eq("account_id", account.getId()).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();
        for(var t : ioMoneyHistory) {
            balance += t.getCount();
        }

        // box tax transactions
        List<KtsBoxTax> boxTax = boxTaxDao.queryBuilder().where()
                .eq("account_id", account.getId()).and()
                .ne("count", 0).and()
                .gt("created_time", opt.getLastChangedTime()).and()
                .le("created_time", fixedTime).and()
                .eq("rollback", false)
                .query();

        for(var t : boxTax) {
            balance -= t.getCount();
        }
        opt.setBalance(opt.getBalance() + balance);
        opt.setLastChangedTime(fixedTime);
        KtsOptimizedBalance.DAO.createOrUpdate(opt);
        return opt.getBalance();
    }
}
