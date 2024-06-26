package kts.dev.ktsbk.server.periodic;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import kts.dev.Main;
import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.box.KtsBoxTax;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.users.KtsWarn;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.csv.VirtualCsvWriter;
import kts.dev.ktsbk.server.dao.accounts.KtsAccountDao;
import kts.dev.ktsbk.server.dao.box.KtsBoxTaxDao;
import kts.dev.ktsbk.server.dao.bridge.KtsUserAccountDao;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class PeriodicManager {
    private static final KtsBoxTaxDao boxTaxDao = KtsDbSource.INSTANCE.createDao(KtsBoxTax.class);
    private static final KtsUserAccountDao usrAccDao = KtsDbSource.INSTANCE.createDao(KtsUserAccount.class);
    private static final KtsAccountDao accDao = KtsDbSource.INSTANCE.createDao(KtsAccount.class);
    private static final KtsUserDao usrDao = KtsDbSource.INSTANCE.createDao(KtsUser.class);
    private static final Dao<KtsBox, Long> boxDao = KtsDbSource.INSTANCE.createDao(KtsBox.class);
    public static void every1hourBoxTax() {
        try(CloseableIterator<KtsBox> ci = boxDao.iterator(
                boxDao.queryBuilder().orderBy("account_id", true).where()
                        .le("last_tax_time", Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)))
                        .and()
                        .eq("disabled", false)
                        .and()
                        .eq("blocked", false)
                        .prepare()
        )) {
            KtsBox box;
            Timestamp now = Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES));
            while (ci.hasNext()) {
                box = ci.next();

                if(box.getAccount().getCurrency().getBoxRent() == 0) {
                    box.setLastTaxTime(now);
                    boxDao.update(box);
                    continue;
                }

                box.getAccount().loadBalance();
                if(box.getAccount().getBalance() - box.getAccount().getCurrency().getBoxRent() < 0) {
                    box.setBlocked(true);
                    boxDao.update(box);

                    String msg = "Block box notify:\n" +
                            "```diff\n" +
                            "- box_id: " + box.getId() + "\n" +
                            "- item: " + box.getCountPerTransaction() + " " + box.getMinecraftIdentifier() + "\n" +
                            "- status: blocked\n" +
                            "```";

                    Main.service.dn.threadSend(box.getAccount().getUser(), msg);
                    Main.service.dn.threadSend(usrAccDao.getMemberShip(box.getAccount()), msg);
                } else {
                    KtsBoxTax taxRecord = new KtsBoxTax();
                    taxRecord.setAccount(box.getAccount());
                    taxRecord.setCount(box.getAccount().getCurrency().getBoxRent());
                    boxTaxDao.create(taxRecord);
                    box.setLastTaxTime(now);
                    boxDao.update(box);

                    String msg = "Box tax:\n" +
                            "```diff\n" +
                            "+ box_id: " + box.getId() + "\n" +
                            "+ item: " + box.getCountPerTransaction() + " " + box.getMinecraftIdentifier() + "\n" +
                            "+ tax: " + box.getAccount().getCurrency().getBoxRent() + "\n" +
                            "+ status: active\n" +
                            "```";

                    Main.service.dn.threadSend(box.getAccount().getUser(), msg);
                    Main.service.dn.threadSend(usrAccDao.getMemberShip(box.getAccount()), msg);

                }
            }
        } catch (Exception ignore) {}
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH");


    public static void every3daysAtFixTimeBoxTax() {
        Instant iNow = Instant.now();
        Timestamp now = Timestamp.from(iNow);
        try {
            CloseableIterator<KtsAccount> iterator = accDao.iterator();
            while(iterator.hasNext()) {
                KtsAccount acc = iterator.next();
                acc.loadBalance();
                List<KtsBox> boxes = boxDao.queryBuilder().where()
                        .eq("account_id", acc.getId())
                        .and()
                        .eq("disabled", false)
                        .and()
                        .eq("blocked", false).query();
                if(boxes.isEmpty()) continue;
                VirtualCsvWriter w = new VirtualCsvWriter(acc.getName() + "_box_tax_" + sdf.format(Date.from(iNow)) + ".csv", "box_id", "count", "item", "tax", "status");
                for(int i = 0; i < boxes.size(); i++) {
                    KtsBox box = boxes.get(i);
                    if(acc.getBalance() - acc.getCurrency().getBoxRent() < 0) {
                        box.setBlocked(true);
                        boxDao.update(box);
                        w.printer.printRecord(box.getId(), box.getCountPerTransaction(), box.getMinecraftIdentifier(), acc.getCurrency().getBoxRent(), "-");
                    } else {
                        KtsBoxTax taxRecord = new KtsBoxTax();
                        taxRecord.setAccount(box.getAccount());
                        taxRecord.setCount(box.getAccount().getCurrency().getBoxRent());
                        boxTaxDao.create(taxRecord);
                        box.setLastTaxTime(now);
                        boxDao.update(box);

                        w.printer.printRecord(box.getId(), box.getCountPerTransaction(), box.getMinecraftIdentifier(), acc.getCurrency().getBoxRent(), "+");
                    }
                }
                Main.service.dn.sendFiles(acc.getUser(), w.getFileUpload());
                w.close();
            }
            iterator.close();
        } catch (Exception ignored) {}
    }



    public static void every1dayWarnCheck() {
        try {
            List<KtsWarn> warns = KtsWarn.DAO.queryBuilder().where().lt("before", Timestamp.from(Instant.now())).and().eq("banned", false).query();
            for(KtsWarn warn : warns) {
                usrDao.refresh(warn.getGuilty());
                warn.getGuilty().setBlocked(true);
                warn.setBanned(true);
                String message = "You banned by:\n" +
                        "```diff\n" +
                        "warn_id: " + warn.getId() + "\n" +
                        "```";
                KtsWarn.DAO.update(warn);
                usrDao.update(warn.getGuilty());
                Main.service.dn.threadSend(warn.getGuilty(), message);
            }
        } catch (SQLException ignore) {}
    }
}
