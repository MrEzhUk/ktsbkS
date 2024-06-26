package kts.dev.ktsbk.server;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.*;
import kts.dev.ktsbk.common.db.accounts.*;
import kts.dev.ktsbk.common.db.box.KtsBoxTax;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.box.KtsBoxType;
import kts.dev.ktsbk.common.db.currencies.KtsCurrency;
import kts.dev.ktsbk.common.db.currencies.KtsCurrencyPermissions;
import kts.dev.ktsbk.common.db.multiworld.KtsServer;
import kts.dev.ktsbk.common.db.multiworld.KtsWorld;
import kts.dev.ktsbk.common.db.users.KtsPermission;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;
import kts.dev.ktsbk.common.db.bridge.KtsUserPermission;
import kts.dev.ktsbk.common.db.users.KtsWarn;
import kts.dev.ktsbk.common.services.KtsBkServiceC2S;
import kts.dev.ktsbk.common.utils.*;
import kts.dev.ktsbk.ktsbkbot.KtsBkDiscordNotify;
import kts.dev.ktsbk.server.auth.PasswordGenerator;
import kts.dev.ktsbk.server.auth.PermissionManager;
import kts.dev.ktsbk.server.dao.box.KtsBoxTaxDao;
import kts.dev.ktsbk.server.dao.accounts.KtsAccountDao;
import kts.dev.ktsbk.server.dao.bridge.KtsUserAccountDao;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;
import kts.dev.ktsbk.server.periodic.PeriodicManager;

import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class KtsBkServiceC2SImpl implements KtsBkServiceC2S {
    private final PermissionManager permissionManager = new PermissionManager();
    private final PasswordGenerator gen = new PasswordGenerator.PasswordGeneratorBuilder()
            .useDigits(true)
            .useLower(true)
            .useUpper(true)
            .build();

    public final KtsBkDiscordNotify dn = new KtsBkDiscordNotify();

    KtsAccountDao accDao;
    KtsBoxTaxDao boxTaxDao;
    KtsUserDao usrDao;
    KtsUserAccountDao usrAccDao;

    public KtsBkServiceC2SImpl() {
        permissionManager.addPermission("ktsbk.create.user");
        permissionManager.addPermission("ktsbk.delete.user");
        permissionManager.addPermission("ktsbk.block.user");
        permissionManager.addPermission("ktsbk.unblock.user");
        permissionManager.addPermission("ktsbk.password.reset");

        permissionManager.addPermission("ktsbk.perm.see");
        permissionManager.addPermission("ktsbk.perm.give");
        permissionManager.addPermission("ktsbk.perm.take");
        permissionManager.addPermission("ktsbk.perm.auto");

        permissionManager.addPermission("ktsbk.create.server");
        permissionManager.addPermission("ktsbk.block.server");
        permissionManager.addPermission("ktsbk.unblock.server");

        permissionManager.addPermission("ktsbk.create.world");
        permissionManager.addPermission("ktsbk.create.currency");
        permissionManager.addPermission("ktsbk.create.account");
        permissionManager.addPermission("ktsbk.disable.account");
        permissionManager.addPermission("ktsbk.block.account");

        permissionManager.addPermission("ktsbk.io.money");
        permissionManager.addPermission("ktsbk.give.warn");
        permissionManager.addPermission("ktsbk.take.warn");

        accDao = KtsDbSource.INSTANCE.createDao(KtsAccount.class);
        boxTaxDao = KtsDbSource.INSTANCE.createDao(KtsBoxTax.class);
        usrDao = KtsDbSource.INSTANCE.createDao(KtsUser.class);
        usrAccDao = KtsDbSource.INSTANCE.createDao(KtsUserAccount.class);

        Calendar calendar = Calendar.getInstance();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // scheduler.scheduleAtFixedRate(PeriodicManager::every1hourBoxTax, 0, 1, TimeUnit.HOURS);
        long offset = TimeUnit.DAYS.toSeconds(3)
                - TimeUnit.DAYS.toSeconds(calendar.get(Calendar.DAY_OF_YEAR) % 3)
                - TimeUnit.HOURS.toSeconds(calendar.get(Calendar.HOUR))
                - TimeUnit.MINUTES.toSeconds(calendar.get(Calendar.MINUTE))
                - TimeUnit.SECONDS.toSeconds(calendar.get(Calendar.SECOND));
        //System.out.println(offset);
        scheduler.scheduleAtFixedRate(PeriodicManager::every3daysAtFixTimeBoxTax, offset, TimeUnit.DAYS.toSeconds(3), TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(PeriodicManager::every1dayWarnCheck, 0, 1, TimeUnit.DAYS);

        //Timer time = new Timer();
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        //calendar.set(Calendar.HOUR_OF_DAY, 23);
        //time.scheduleAtFixedRate(PeriodicManager::every1hourBoxTax, );

    }

    @Override
    public KbErr createUser(AuthContext auth, String nickname, String disid) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.create.user")) {
            return KbErr.PERMISSION_ERROR;
        }

        try {
            SelectArg argNickname = new SelectArg();
            argNickname.setValue(nickname);
            List<KtsUser> l_users = usrDao.queryForEq("nickname", argNickname);

            if(!l_users.isEmpty()) {
                if(l_users.get(0).isDisabled()) {
                    return KbErr.USER_DISABLED;
                } else {
                    return KbErr.USER_ALREADY_EXISTS;
                }
            }

            String token = nickname +  gen.generate(128);

            KtsUser n_usr = new KtsUser();
            n_usr.setDisId(disid);
            n_usr.setNickname(nickname);
            n_usr.setToken(token);
            usrDao.create(n_usr);
            this.dn.threadSend(n_usr, "Token:\n||`" + token + "`||");
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr resetPassword(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.password.reset")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return KbErr.USER_NOT_FOUND;
            String token = usr.getNickname() + gen.generate(128);
            usr.setToken(token);
            usrDao.update(usr);
            this.dn.threadSend(usr, "Token:\n||`" + token + "`||");
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr disableUser(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.delete.user")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return KbErr.USER_NOT_FOUND;
            usr.setDisabled(true);
            usrDao.update(usr);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr restoreUser(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.delete.user")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return KbErr.USER_NOT_FOUND;
            usr.setDisabled(false);
            usrDao.update(usr);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr blockUser(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.block.user")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return KbErr.USER_NOT_FOUND;
            usr.setBlocked(true);
            usrDao.update(usr);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr unblockUser(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.block.user")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return KbErr.USER_NOT_FOUND;
            usr.setBlocked(false);
            usrDao.update(usr);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<List<KtsUser>> autoCompleteUser(AuthContext auth, String end) {
        end = end.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsUser> users = usrDao.queryBuilder().limit(6L).orderBy("nickname", true)
                    .where().like("nickname", "%" + end + "%").query();

            for(KtsUser user : users) {
                user.setToken(null);
                //user.setDisId(null);
            }
            return new WithKbErr<>(KbErr.SUCCESS, users);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public WithKbErr<KtsUser> getUserById(AuthContext auth, long userId) {
        try {
            KtsUser usr = usrDao.queryForId(userId);
            if(usr == null) return new WithKbErr<>(KbErr.USER_NOT_FOUND, null);
            usr.setToken("");
            return new WithKbErr<>(KbErr.SUCCESS, usr);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }

    }

    @Override
    public WithKbErr<List<KtsPermission>> seeUserPermissions(AuthContext auth, long userId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.perm.see")) {
            return new WithKbErr<>(KbErr.PERMISSION_ERROR, null);
        }
        try {
            QueryBuilder<KtsUserPermission, Long> sub_query = KtsUserPermission.DAO.queryBuilder();
            sub_query.where().eq("user_id", userId);
            List<KtsPermission> arr = KtsPermission.DAO.queryBuilder().join(sub_query).query();
            return new WithKbErr<>(KbErr.SUCCESS, arr);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public KbErr givePermission(AuthContext auth, long userId, long permissionId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.perm.give")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUserPermission kup = KtsUserPermission.DAO.queryBuilder().where()
                    .eq("user_id", userId)
                    .and()
                    .eq("permission_id", permissionId)
                    .queryForFirst();

            if(kup != null) return KbErr.SUCCESS;
            kup = new KtsUserPermission();
            KtsUser usr = usrDao.queryForId(userId);
            KtsPermission perm = KtsPermission.DAO.queryForId(permissionId);

            if(usr == null) return KbErr.USER_NOT_FOUND;
            if(perm == null) return KbErr.PERM_NOT_FOUND;

            kup.setUser(usr);
            kup.setPermission(perm);
            KtsUserPermission.DAO.create(kup);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr takePermission(AuthContext auth, long userId, long permissionId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.perm.take")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUserPermission kup = KtsUserPermission.DAO.queryBuilder().where()
                    .eq("user_id", userId)
                    .and()
                    .eq("permission_id", permissionId)
                    .queryForFirst();
            if(kup == null) return KbErr.PERM_NOT_FOUND;
            KtsUserPermission.DAO.delete(kup);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<List<KtsPermission>> autoCompletePermission(AuthContext auth, String end) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.perm.auto")) {
            return new WithKbErr<>(KbErr.PERMISSION_ERROR, null);
        }
        end = end.replaceAll("[^_a-zA-Z0-9]", "");

        try {
            List<KtsPermission> perms = KtsPermission.DAO.queryBuilder().limit(10L).orderBy("permission_string", true)
                    .where().like("permission_string", end + "%").query();

            return new WithKbErr<>(KbErr.SUCCESS, perms);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public KbErr createServer(AuthContext auth, InetAddress ip, String name, String shortName) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.create.server")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            SelectArg nameArg = new SelectArg();
            SelectArg shortNameArg = new SelectArg();
            nameArg.setValue(name);
            shortNameArg.setValue(shortName);
            KtsServer serv = KtsServer.DAO.queryBuilder().where().eq("name", nameArg).or().eq("short_name", shortNameArg).queryForFirst();
            if(serv != null) {
                if(serv.isDisabled()) return KbErr.SERVER_DISABLED;
                else return KbErr.SERVER_ALREADY_EXISTS;
            }
            serv = new KtsServer();
            serv.setIp(ip.getHostAddress());
            serv.setName(name);
            serv.setShortName(shortName);
            KtsServer.DAO.create(serv);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }

    }

    @Override
    public KbErr blockServer(AuthContext auth, long serverId) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public KbErr unblockServer(AuthContext auth, long serverId) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public WithKbErr<List<KtsServer>> autoCompleteServer(AuthContext auth, String end) {
        end = end.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsServer> servers = KtsServer.DAO.queryBuilder().limit(3L).orderBy("name", true)
                    .where()
                    .like("name", "%" + end + "%")
                    .or()
                    .like("short_name", "%" + end + "%")
                    .query();

            return new WithKbErr<>(KbErr.SUCCESS, servers);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public KbErr createWorld(AuthContext auth, long serverId, String mcName, String ktsbkName) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.create.world")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            SelectArg mcArg = new SelectArg();
            mcArg.setValue(mcName);
            SelectArg ktsbkArg = new SelectArg();
            ktsbkArg.setValue(ktsbkName);
            QueryBuilder<KtsWorld, Long> q_builder = KtsWorld.DAO.queryBuilder();
            Where<KtsWorld, Long> where = q_builder.where();
            q_builder.setWhere(where.and(
                    where.eq("server_id", serverId),
                    where.eq("minecraft_name", mcArg).or().eq("ktsbk_name", ktsbkArg)
            ));
            KtsWorld world = q_builder.queryForFirst();

            if(world != null) return KbErr.WORLD_ALREADY_EXISTS;

            KtsServer server = KtsServer.DAO.queryForId(serverId);
            if(server == null) return KbErr.SERVER_NOT_FOUND;
            if(server.isDisabled()) return KbErr.SERVER_DISABLED;

            world = new KtsWorld();

            world.setServer(server);
            world.setMcName(mcName);
            world.setKtsbkName(ktsbkName);

            KtsWorld.DAO.create(world);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<KtsWorld> getWorldById(AuthContext auth, long worldId) {
        try {
            KtsWorld world = KtsWorld.DAO.queryForId(worldId);
            if(world == null) return new WithKbErr<>(KbErr.WORLD_NOT_FOUND, null);
            return new WithKbErr<>(KbErr.SUCCESS, world);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public WithKbErr<KtsWorld> getWorldByKtsBkName(AuthContext auth, String ktsbk_string) {
        if(ktsbk_string.length() > 1000) return new WithKbErr<>(KbErr.WORLD_NOT_FOUND);
        String end = ktsbk_string.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsWorld> world = KtsWorld.DAO.queryForEq("ktsbk_name", end);
            if(world.isEmpty()) return new WithKbErr<>(KbErr.WORLD_NOT_FOUND, null);
            return new WithKbErr<>(KbErr.SUCCESS, world.get(0));
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public WithKbErr<List<KtsWorld>> autoCompleteWorld(AuthContext auth, String end) {
        end = end.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsWorld> worlds = KtsWorld.DAO.queryBuilder().limit(6L).orderBy("ktsbk_name", true)
                    .where()
                    .like("ktsbk_name", "%" + end + "%")
                    .query();

            return new WithKbErr<>(KbErr.SUCCESS, worlds);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public KbErr createCurrency(AuthContext auth, long serverId, String name, String short_name) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.create.currency")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            SelectArg mcArg = new SelectArg();
            mcArg.setValue(name);
            SelectArg ktsbkArg = new SelectArg();
            ktsbkArg.setValue(short_name);
            QueryBuilder<KtsCurrency, Long> q_builder = KtsCurrency.DAO.queryBuilder();
            Where<KtsCurrency, Long> where = q_builder.where();
            q_builder.setWhere(where.and(
                    where.eq("server_id", serverId),
                    where.eq("name", mcArg).or().eq("short_name", ktsbkArg)
            ));
            KtsCurrency currency = q_builder.queryForFirst();

            if(currency != null) {
                if(currency.isDisabled()) return KbErr.CURRENCY_DISABLED;
                else return KbErr.CURRENCY_ALREADY_EXISTS;
            }
            KtsServer server = KtsServer.DAO.queryForId(serverId);
            if(server == null) return KbErr.SERVER_NOT_FOUND;
            if(server.isDisabled()) return KbErr.SERVER_DISABLED;

            currency = new KtsCurrency();

            currency.setServer(server);
            currency.setName(name);
            currency.setShortName(short_name);

            KtsCurrency.DAO.create(currency);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr setCurrencyTransactionPercent(AuthContext auth, long currencyId, long percent) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public KbErr setCurrencyBoxRent(AuthContext auth, long currencyId, long rent) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public WithKbErr<List<KtsCurrency>> autoCompleteCurrency(AuthContext auth, String end) {
        end = end.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsCurrency> worlds = KtsCurrency.DAO.queryBuilder().limit(6L).orderBy("name", true)
                    .where()
                    .like("short_name", "%" + end + "%")
                    .query();

            return new WithKbErr<>(KbErr.SUCCESS, worlds);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public KbErr createAccount(AuthContext auth, String name, long currencyId) {
        if(name.length() > 16) return KbErr.MAX_ACCOUNT_LENGTH;
        if(name.length() < 3) return KbErr.MIN_ACCOUNT_LENGTH;
        if(!name.matches("[_0-9a-zA-Z]+")) return KbErr.ILLEGAL_NAME;

        SelectArg nameArg = new SelectArg();
        nameArg.setValue(name);
        try {
            long user_account_count = accDao.queryBuilder().where()
                    .eq("user_id", auth.getUser().getId())
                    .and()
                    .eq("disabled", false)
                    .countOf();
            KtsCurrency currency = KtsCurrency.DAO.queryForId(currencyId);

            if(currency == null) return KbErr.CURRENCY_NOT_FOUND;
            if(currency.isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(currency.isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(currency.getServer().isBlocked()) return KbErr.SERVER_BLOCKED;
            if(currency.getServer().isDisabled()) return KbErr.SERVER_DISABLED;

            if(
                    (currency.getCurrencyPermission() & KtsCurrencyPermissions.PERM_CREATE_ACCOUNT.spec()) == 0 &&
                    !permissionManager.checkCurrencyPermission(auth.getUser(), currency.getId(), "create_account")
            ) {
                return KbErr.PERMISSION_ERROR;
            }

            if(user_account_count > currency.getMaxAccounts()) return KbErr.MAX_ACCOUNT_LIMIT;
            List<KtsAccount> accounts = accDao.queryForEq("name", nameArg);
            if(!accounts.isEmpty()) {
                if(accounts.get(0).isDisabled()) return KbErr.ACCOUNT_DISABLED;
                else return KbErr.ACCOUNT_ALREADY_EXISTS;
            }
            KtsAccount account = new KtsAccount();
            account.setName(name);
            account.setUser(auth.getUser());
            account.setCurrency(currency);
            accDao.create(account);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr blockAccount(AuthContext auth, long accountId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.block.account")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser account = usrDao.queryForId(accountId);
            if(account == null) return KbErr.USER_NOT_FOUND;
            account.setBlocked(true);
            usrDao.update(account);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr unblockAccount(AuthContext auth, long accountId) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.unblock.account")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser account = usrDao.queryForId(accountId);
            if(account == null) return KbErr.USER_NOT_FOUND;
            account.setBlocked(false);
            usrDao.update(account);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr disableAccount(AuthContext auth, long accountId) {
        try {
            KtsAccount account = accDao.queryForId(accountId);
            if(account == null) return KbErr.ACCOUNT_NOT_FOUND;
            if(account.getUser().getId() != auth.getUser().getId()) return KbErr.PERMISSION_ERROR;
            account.setDisabled(true);
            accDao.update(account);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr restoreAccount(AuthContext auth, long accountId) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public KbErr holdAccountMoney(AuthContext auth, long accountId, long count) {
        return KbErr.NOT_IMPLEMENTED;
    }

    @Override
    public WithKbErr<List<KtsAccount>> autoCompleteAccount(AuthContext auth, String end) {
        end = end.replaceAll("[^_a-zA-Z0-9]", "");
        try {
            List<KtsAccount> accounts = accDao.queryBuilder().limit(6L).orderBy("name", true)
                    .where().like("name", "%" + end + "%").and().eq("disabled", false).query();

            return new WithKbErr<>(KbErr.SUCCESS, accounts);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public WithKbErr<KtsAccount> getAccountById(AuthContext auth, long accountId) {
        try {
            KtsAccount acc = accDao.queryForId(accountId);
            if(acc == null) return new WithKbErr<>(KbErr.ACCOUNT_NOT_FOUND, null);
            if(
                    acc.getUser().getId() == auth.getUser().getId() ||
                            usrAccDao.isMemberShip(acc, auth.getUser())
            ) {
                acc.loadBalance();
            }
            return new WithKbErr<>(KbErr.SUCCESS, acc);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }

    @Override
    public WithKbErr<List<KtsAccount>> getMyAccounts(AuthContext auth) {
        try {
            List<KtsAccount> accounts = accDao.queryBuilder().orderBy("currency_id", true)
                    .where().eq("user_id", auth.getUser().getId()).query();
            for(KtsAccount acc : accounts) acc.loadBalance();
            //System.out.println(accounts);
            return new WithKbErr<>(KbErr.SUCCESS, accounts);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION, null);
        }
    }
    @Override
    public WithKbErr<List<KtsAccount>> getMyMembership(AuthContext auth) {
        try {
            List<KtsAccount> accounts = usrAccDao.getMemberShip(auth.getUser());
            for(KtsAccount acc : accounts) {
                acc.loadBalance();
            }
            return new WithKbErr<>(KbErr.SUCCESS, accounts);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public WithKbErr<List<KtsUser>> getAccountMembership(AuthContext auth, long accountId) {
        try {
            KtsAccount account = accDao.queryForId(accountId);
            if(account == null) return new WithKbErr<>(KbErr.ACCOUNT_NOT_FOUND);
            List<KtsUser> users = usrAccDao.getMemberShip(account);
            return new WithKbErr<>(KbErr.SUCCESS, users);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public KbErr pay(AuthContext auth, long accountId1, long accountId2, long count, String msg) {
        if(count <= 0) return KbErr.ILLEGAL_COUNT;
        if(msg == null) msg = "";

        try {
            KtsAccount acc1 = accDao.queryForId(accountId1);
            KtsAccount acc2 = accDao.queryForId(accountId2);
            if(acc1 == null || acc2 == null) return KbErr.ACCOUNT_NOT_FOUND;

            if(acc1.getId() == acc2.getId()) return KbErr.ACCOUNT_EQUAL_ACCOUNT;

            if(!usrAccDao.isMemberShip(acc1, auth.getUser()) && acc1.getUser().getId() != auth.getUser().getId()) return KbErr.PERMISSION_ERROR;
            if(
                    (acc1.getCurrency().getCurrencyPermission() & KtsCurrencyPermissions.PERM_PAY_ACCOUNT.spec()) == 0 &&
                    !permissionManager.checkCurrencyPermission(auth.getUser(), acc1.getCurrency().getId(), "pay")
            ) return KbErr.PERMISSION_ERROR;

            if(acc1.isDisabled() || acc2.isDisabled()) return KbErr.ACCOUNT_DISABLED;
            if(acc1.isBlocked()) return KbErr.ACCOUNT_BLOCKED;
            if(acc1.getCurrency().isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(acc1.getCurrency().isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(acc1.getCurrency().getServer().isDisabled()) return KbErr.SERVER_DISABLED;
            if(acc1.getCurrency().getServer().isBlocked()) return KbErr.SERVER_BLOCKED;

            if(acc1.getCurrency().getId() != acc2.getCurrency().getId()) return KbErr.DIFFERENT_CURRENCIES;
            acc1.loadBalance();
            if(acc1.getBalance() - count < 0) return KbErr.NO_MONEY;

            KtsAccountPayHistory k_aph = new KtsAccountPayHistory();
            k_aph.setDoer(auth.getUser());
            k_aph.setFromAccount(acc1);
            k_aph.setToAccount(acc2);
            k_aph.setCount(count);
            k_aph.setMsg(msg.substring(0, (int) Long.min(msg.length(), 512L)));
            KtsAccountPayHistory.DAO.create(k_aph);


            String message = "Pay Transaction:\n" +
                    "```diff\n" +
                    "- " + acc1.getName() + "\n" +
                    "+ " + acc2.getName() + "\n" +
                    "pay_id: " + k_aph.getId() + "\n" +
                    "doer: " + auth.getUser().getNickname() + "\n" +
                    "sum: " + count + " " + acc1.getCurrency().getShortName() + "\n" +
                    "message: " + msg + "\n" +
                    "```\n";

            List<KtsUser> users = new ArrayList<>();
            users.addAll(usrAccDao.getMemberShip(acc1));
            users.addAll(usrAccDao.getMemberShip(acc2));
            users.add(acc1.getUser());
            users.add(acc2.getUser());

            dn.threadSend(users, message);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr kick(AuthContext auth, long accountId, long userId) {
        KtsAccountDao accDao = KtsDbSource.INSTANCE.createDao(KtsAccount.class);
        try {
            KtsAccount acc = accDao.queryForId(accountId);
            KtsUser usr = usrDao.queryForId(userId);

            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;
            if(usr == null) return KbErr.USER_NOT_FOUND;

            if(acc.getUser().getId() != auth.getUser().getId()) return KbErr.PERMISSION_ERROR;
            if(
                    (acc.getCurrency().getCurrencyPermission() & KtsCurrencyPermissions.PERM_KICK_ACCOUNT.spec()) == 0 &&
                    !permissionManager.checkCurrencyPermission(auth.getUser(), acc.getCurrency().getId(), "kick")
            ) return KbErr.PERMISSION_ERROR;
            if(acc.isBlocked()) return KbErr.ACCOUNT_BLOCKED;
            if(acc.isDisabled()) return KbErr.ACCOUNT_DISABLED;
            if(acc.getCurrency().isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(acc.getCurrency().isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(acc.getCurrency().getServer().isDisabled()) return KbErr.SERVER_DISABLED;
            if(acc.getCurrency().getServer().isBlocked()) return KbErr.SERVER_BLOCKED;

            if(!usrAccDao.isMemberShip(acc, usr)) return KbErr.SUCCESS;
            usrAccDao.delete(usrAccDao.queryForEq("user_id", usr.getId()));
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr invite(AuthContext auth, long accountId, long userId) {
        try {
            KtsAccount acc = accDao.queryForId(accountId);
            KtsUser usr = usrDao.queryForId(userId);

            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;
            if(usr == null) return KbErr.USER_NOT_FOUND;
            if(usr.getId() == auth.getUser().getId()) return KbErr.USER_ALREADY_EXISTS;

            if(acc.getUser().getId() != auth.getUser().getId()) return KbErr.PERMISSION_ERROR;
            if(
                    (acc.getCurrency().getCurrencyPermission() & KtsCurrencyPermissions.PERM_INVITE_ACCOUNT.spec()) == 0 &&
                    !permissionManager.checkCurrencyPermission(auth.getUser(), acc.getCurrency().getId(), "invite")
            ) return KbErr.PERMISSION_ERROR;
            if(acc.isBlocked()) return KbErr.ACCOUNT_BLOCKED;
            if(acc.isDisabled()) return KbErr.ACCOUNT_DISABLED;
            if(acc.getCurrency().isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(acc.getCurrency().isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(acc.getCurrency().getServer().isDisabled()) return KbErr.SERVER_DISABLED;
            if(acc.getCurrency().getServer().isBlocked()) return KbErr.SERVER_BLOCKED;
            if(usrAccDao.isMemberShip(acc, usr)) return KbErr.SUCCESS;

            KtsUserAccount kua = new KtsUserAccount();
            kua.setUser(usr);
            kua.setAccount(acc);
            usrAccDao.create(kua);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr io_money(AuthContext auth, long accountId, long count) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.io.money")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsAccount acc = accDao.queryForId(accountId);
            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;
            KtsAccountIOMoneyHistory ioMoneyHistory = new KtsAccountIOMoneyHistory();
            ioMoneyHistory.setAccount(acc);
            ioMoneyHistory.setDoer(auth.getUser());
            ioMoneyHistory.setCount(count);
            KtsAccountIOMoneyHistory.DAO.create(ioMoneyHistory);
            String message = "I/O money notify: \n" +
                    "```diff\n" +
                    "+ io_money_id: " + ioMoneyHistory.getId() + "\n" +
                    "+ account: " + ioMoneyHistory.getAccount().getName() + "\n" +
                    "+ count: " + ioMoneyHistory.getCount() + "\n";
            dn.threadSend(acc.getUser(), message);
            dn.threadSend(usrAccDao.getMemberShip(acc), message);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr createBox(AuthContext auth, long x, long y, long z, long worldId, long accountId, String itemIdentifier, String itemSerializable, long countPerTransaction, long buyCostPerTransaction, long sellCostPerTransaction, KtsBoxType type) {
        try {
            if(buyCostPerTransaction <= 0 || sellCostPerTransaction <= 0) return KbErr.ILLEGAL_COUNT;
            if(countPerTransaction <= 0) return KbErr.ILLEGAL_COST;
            KtsWorld world = KtsWorld.DAO.queryForId(worldId);
            KtsAccount acc = accDao.queryForId(accountId);
            if(world == null) return KbErr.WORLD_NOT_FOUND;
            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;

            if(acc.getUser().getId() != auth.getUser().getId() && !usrAccDao.isMemberShip(acc, auth.getUser())) return KbErr.PERMISSION_ERROR;
            if(
                    (acc.getCurrency().getCurrencyPermission() & KtsCurrencyPermissions.PERM_CREATE_BOX.spec()) == 0 &&
                    !permissionManager.checkCurrencyPermission(auth.getUser(), acc.getCurrency().getId(), "create_box")
            ) return KbErr.PERMISSION_ERROR;

            if((acc.getCurrency().getBoxTypeSupport() & type.spec()) == 0) return KbErr.BOX_TYPE_NOT_SUPPORT_CURRENCY;

            KtsBox box = KtsBox.DAO.queryBuilder().where().eq("x", x).and().eq("y", y).and().eq("z", z).and().eq("disabled", false).queryForFirst();
            if(box != null) return KbErr.SIDE_BUY_BOX_ALREADY_EXISTS;
            box = new KtsBox();

            box.setX(x);
            box.setY(y);
            box.setZ(z);
            box.setWorld(world);

            box.setAccount(acc);
            box.setMinecraftIdentifier(itemIdentifier);
            box.setMinecraftSerializedItem(itemSerializable);

            box.setBoxType(type);

            box.setCountPerTransaction(countPerTransaction);
            box.setBuyCostPerTransaction(buyCostPerTransaction);
            box.setSellCostPerTransaction(sellCostPerTransaction);
            box.setBlocked(true);

            KtsBox.DAO.create(box);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<List<KtsBox>> getMyBoxes(AuthContext auth) {
        try {
            QueryBuilder<KtsAccount, Long> ua_builder = accDao.queryBuilder();
            ua_builder.where().eq("user_id", auth.getUser().getId());
            List<KtsBox> l = KtsBox.DAO.queryBuilder()
                    .join(ua_builder)
                    .where()
                    .eq("account_id", new ColumnArg("accounts", "id"))
                    .query();
            List<KtsAccount> accounts = usrAccDao.getMemberShip(auth.getUser());
            List<KtsBox> p;
            for(KtsAccount acc : accounts) {
                p = KtsBox.DAO.queryForEq("account_id", acc.getId());
                l.addAll(p);
            }
            return new WithKbErr<>(KbErr.SUCCESS, l);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public KbErr buyInBox(AuthContext auth, long account_id, long box_id, long count_transactions) {
        if(count_transactions <= 0) return KbErr.ILLEGAL_COUNT;
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            KtsAccount acc = accDao.queryForId(account_id);

            if(box == null) return KbErr.SIDE_BUY_BOX_NOT_FOUND;
            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;

            if(!box.getBoxType().isBuy()) return KbErr.ERROR_BOX_ACTION_TYPE;


            if(box.getAccount().getId() == account_id) return KbErr.ACCOUNT_EQUAL_ACCOUNT;
            if(box.isBlocked()) return KbErr.SIDE_BUY_BOX_NOT_FOUND;
            if(box.isDisabled()) return KbErr.SIDE_BUY_BOX_NOT_FOUND;

            if(box.getAccount().getCurrency().getId() != acc.getCurrency().getId()) return KbErr.DIFFERENT_CURRENCIES;
            if(acc.getUser().getId() != auth.getUser().getId() && !usrAccDao.isMemberShip(acc, auth.getUser())) return KbErr.PERMISSION_ERROR;

            if(acc.isDisabled()) return KbErr.ACCOUNT_DISABLED;
            if(acc.isBlocked()) return KbErr.ACCOUNT_BLOCKED;
            if(acc.getCurrency().isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(acc.getCurrency().isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(acc.getCurrency().getServer().isDisabled()) return KbErr.SERVER_DISABLED;
            if(acc.getCurrency().getServer().isBlocked()) return KbErr.SERVER_BLOCKED;

            long sum = box.getBuyCostPerTransaction() * count_transactions;
            long tax = (long)Math.ceil(sum * acc.getCurrency().getTransactionPercent() / 100.0);
            acc.loadBalance();
            if(sum + tax > acc.getBalance()) return KbErr.NO_MONEY;

            KtsAccountBuyHistory record = new KtsAccountBuyHistory();
            record.setBox(box);
            record.setAccount(acc);
            record.setDoer(auth.getUser());
            record.setCount(count_transactions);
            record.setCost(box.getBuyCostPerTransaction());
            record.setTax(tax);

            KtsAccountBuyHistory.DAO.create(record);

            box.setCountNow(box.getCountNow() - count_transactions);
            KtsBox.DAO.update(box);

            String message = "Buy Transaction:\n" +
                    "```diff\n" +
                    "- " + acc.getName() + "\n" +
                    "+ " + box.getAccount().getName() + "\n" +
                    "buy_id: " + record.getId() + "\n" +
                    "doer: " + auth.getUser().getNickname() + "\n" +
                    "sum: " + sum + " " + acc.getCurrency().getShortName() + "\n" +
                    "tax: " + tax + "\n" +
                    "item: " + count_transactions + "*" + box.getCountPerTransaction() + " " + box.getMinecraftIdentifier() + "\n" +
                    "```\n";

            dn.threadSend(acc.getUser(), message);
            dn.threadSend(usrAccDao.getMemberShip(acc), message);
            dn.threadSend(box.getAccount().getUser(), message);
            dn.threadSend(usrAccDao.getMemberShip(box.getAccount()), message);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }

    }

    @Override
    public KbErr sellInBox(AuthContext auth, long account_id, long box_id, long count_transactions) {
        if(count_transactions <= 0) return KbErr.ILLEGAL_COUNT;
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            KtsAccount acc = accDao.queryForId(account_id);

            if(box == null) return KbErr.SIDE_BUY_BOX_NOT_FOUND;
            if(acc == null) return KbErr.ACCOUNT_NOT_FOUND;

            if(!box.getBoxType().isSell()) return KbErr.ERROR_BOX_ACTION_TYPE;

            if(box.getAccount().getId() == account_id) return KbErr.ACCOUNT_EQUAL_ACCOUNT;
            if(box.isBlocked()) return KbErr.SIDE_BUY_BOX_NOT_FOUND;
            if(box.isDisabled()) return KbErr.SIDE_BUY_BOX_NOT_FOUND;

            if(box.getAccount().getCurrency().getId() != acc.getCurrency().getId()) return KbErr.DIFFERENT_CURRENCIES;
            if(acc.getUser().getId() != auth.getUser().getId() && !usrAccDao.isMemberShip(acc, auth.getUser())) return KbErr.PERMISSION_ERROR;

            if(acc.isDisabled()) return KbErr.ACCOUNT_DISABLED;
            if(acc.isBlocked()) return KbErr.ACCOUNT_BLOCKED;
            if(acc.getCurrency().isDisabled()) return KbErr.CURRENCY_DISABLED;
            if(acc.getCurrency().isBlocked()) return KbErr.CURRENCY_BLOCKED;
            if(acc.getCurrency().getServer().isDisabled()) return KbErr.SERVER_DISABLED;
            if(acc.getCurrency().getServer().isBlocked()) return KbErr.SERVER_BLOCKED;

            long sum = box.getSellCostPerTransaction() * count_transactions;
            long tax = (long)Math.ceil(sum * acc.getCurrency().getTransactionPercent() / 100.0);
            box.getAccount().loadBalance();
            if(sum + tax > box.getAccount().getBalance()) return KbErr.NO_MONEY;

            KtsAccountSellHistory record = new KtsAccountSellHistory();
            record.setBox(box);
            record.setAccount(acc);
            record.setDoer(auth.getUser());
            record.setCount(count_transactions);
            record.setCost(box.getSellCostPerTransaction());
            record.setTax(tax);

            KtsAccountSellHistory.DAO.create(record);
            box.setCountNow(box.getCountNow() + count_transactions);
            KtsBox.DAO.update(box);

            String message = "Sell Transaction:\n" +
                    "```diff\n" +
                    "- " + acc.getName() + "\n" +
                    "+ " + box.getAccount().getName() + "\n" +
                    "sell_id: " + record.getId() + "\n" +
                    "doer: " + auth.getUser().getNickname() + "\n" +
                    "sum: " + sum + " " + acc.getCurrency().getShortName() + "\n" +
                    "tax: " + tax + "\n" +
                    "item: " + count_transactions + "*" + box.getCountPerTransaction() + " " + box.getMinecraftIdentifier() + "\n" +
                    "```\n";

            dn.threadSend(acc.getUser(), message);
            dn.threadSend(usrAccDao.getMemberShip(acc), message);
            dn.threadSend(box.getAccount().getUser(), message);
            dn.threadSend(usrAccDao.getMemberShip(box.getAccount()), message);

            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<List<KtsBox>> searchBox(AuthContext auth, SearchProduct criteria) {
        if(criteria.getMaxRecommendationCount() <= 0L && criteria.getMaxRecommendationCount() > 32) return new WithKbErr<>(KbErr.ILLEGAL_COUNT);

        List<KtsBox> recommendations = new ArrayList<>();

        try {
            QueryBuilder<KtsWorld, Long> world_builder = KtsWorld.DAO.queryBuilder();

            if(criteria.getWorldId() != 0) {
                world_builder.where().eq("server_id", criteria.getWorldId());
            }

            QueryBuilder<KtsAccount, Long> account_builder = accDao.queryBuilder();
            if(criteria.getCurrencyId() != 0) {
                account_builder.where().eq("currency_id", criteria.getCurrencyId());
            }

            QueryBuilder<KtsBox, Long> qb = KtsBox.DAO.queryBuilder().join(world_builder).join(account_builder).limit(criteria.getMaxRecommendationCount());
            Where<KtsBox, Long> wr = qb.where();
            wr
                    .eq("blocked", false).and()
                    .eq("disabled", false).and()
                    .gt("count_now", 0).and()
                    .isNotNull("last_tax_time").and()
                    .gt("last_tax_time", Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

            if(criteria.getServerId() != 0) {
                wr.and().eq("server_id", criteria.getServerId());
            }

            if(criteria.getMinecraftId() != null && !criteria.getMinecraftId().isEmpty()) {
                wr.and().eq("mc_identifier", criteria.getMinecraftId());
            }

            if(criteria.getMaxBuyCostPerCount() != 0) {
                wr.and().raw("`buy_cost_per_transaction` / `count_per_transaction` <= " + criteria.getMaxBuyCostPerCount());
            }

            if(criteria.getMinCount() != 0) {
                wr.and().raw("`count_now` * `count_per_transaction` >= " + criteria.getMinCount());
            }

            if(criteria.getRadius() != 0) {
                wr.and().raw(
                        "(`x`-("+ criteria.getX() +"))*(`x`-("+ criteria.getX() +")) + " +
                                "(`y`-("+ criteria.getY() +"))*(`y`-("+ criteria.getY() +")) + " +
                                "(`z`-("+ criteria.getZ() +"))*(`z`-("+criteria.getZ()+")) - " +
                                "("+ criteria.getRadius() * criteria.getRadius() +") <= 0"
                );
            }



            qb.setWhere(wr);
            //System.out.println(qb.prepareStatementString());
            CloseableIterator<KtsBox> iterator = KtsBox.DAO.iterator(qb.prepare());
            while(iterator.hasNext()) {
                //System.out.println(iterator.current().getId());
                recommendations.add(iterator.current());
            }
            //System.out.println("!!!");
            iterator.close();
        } catch (Exception ignored) {}
        /*
        KtsBox.DAO.forEach(box -> {
            if(box.isBlocked() || box.isDisabled()) return;
            if(box.getLastTaxTime() == null) return;
            if(box.getLastTaxTime().before(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)))) return;

            if(box.getCountNow() == 0) return;
            if(criteria.getMinecraftId() != null && !criteria.getMinecraftId().isEmpty() && !box.getMinecraftIdentifier().equals(criteria.getMinecraftId())) return;
            if(criteria.getWorldId() != 0 && box.getWorld().getId() != criteria.getWorldId()) return;
            if(criteria.getMinCount() != 0 && box.getCountNow() * box.getCountPerTransaction() < criteria.getMinCount()) return;

            double unit_cost = (double)box.getBuyCostPerTransaction() / box.getCountPerTransaction();
            if(criteria.getMaxBuyCostPerCount() != 0.0 && unit_cost > criteria.getMaxBuyCostPerCount()) return;

            double dist = (box.getX() - criteria.getX()) * (box.getX() - criteria.getX()) +
                    (box.getY() - criteria.getY()) * (box.getY() - criteria.getY()) +
                    (box.getZ() - criteria.getZ()) * (box.getZ() - criteria.getZ());

            if(criteria.getRadius() != 0 && dist > criteria.getRadius() * criteria.getRadius()) return;

            recommendations.add(box);
        });
        if(criteria.getSortedByCost()) {
            recommendations.sort((a, b) -> {
                double delta = (double) a.getBuyCostPerTransaction() / a.getCountPerTransaction() - (double) b.getBuyCostPerTransaction() / b.getCountPerTransaction();
                if (delta > 0) {
                    return 1;
                } else if (delta < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if(criteria.getSortedByNear()) {
            recommendations.sort((a, b) -> {
                double d_a = a.getX()*(a.getX() - 2*criteria.getX()) + a.getY()*(a.getY() - 2*criteria.getY()) + a.getZ()*(a.getZ() - 2*criteria.getZ());
                double d_b = b.getX()*(b.getX() - 2*criteria.getX()) + b.getY()*(b.getY() - 2*criteria.getY()) + b.getZ()*(b.getZ() - 2*criteria.getZ());
                double delta = d_a - d_b;
                if (delta > 0) {
                    return 1;
                } else if (delta < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });
        }

         */
        return new WithKbErr<>(KbErr.SUCCESS, new ArrayList<>(recommendations.subList(0, Integer.min(recommendations.size(), (int)criteria.getMaxRecommendationCount()))));
    }

    @Override
    public WithKbErr<KtsBox> getBoxId(AuthContext auth, long box_id) {
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            if(box == null) return new WithKbErr<>(KbErr.BOX_NOT_FOUND);
            return new WithKbErr<>(KbErr.SUCCESS, box);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public KbErr unblockBox(AuthContext auth, long box_id) {
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            if(box == null) return KbErr.BOX_NOT_FOUND;
            if(box.isDisabled()) return KbErr.BOX_NOT_FOUND;
            if(!box.isBlocked()) return KbErr.SUCCESS;
            if(
                    box.getAccount().getUser().getId() != auth.getUser().getId() &&
                    !usrAccDao.isMemberShip(box.getAccount(), auth.getUser())
            ) {
                return KbErr.PERMISSION_ERROR;
            }
            box.getAccount().loadBalance();
            if(box.getAccount().getBalance() - box.getAccount().getCurrency().getBoxRent() < 0) {
                return KbErr.NO_MONEY;
            }

            if(box.getLastTaxTime() != null && box.getLastTaxTime().after(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)))) {
                box.setBlocked(false);
                KtsBox.DAO.update(box);
                return KbErr.SUCCESS;
            }

            KtsBoxTax record = new KtsBoxTax();
            record.setDoer(auth.getUser());
            record.setAccount(box.getAccount());
            record.setCount(box.getAccount().getCurrency().getBoxRent());
            boxTaxDao.create(record);

            box.setBlocked(false);
            box.setLastTaxTime(Timestamp.from(Instant.now()));
            KtsBox.DAO.update(box);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr blockBox(AuthContext auth, long box_id) {
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            if (box == null) return KbErr.BOX_NOT_FOUND;
            if (box.isDisabled()) return KbErr.BOX_NOT_FOUND;
            if (box.isBlocked()) return KbErr.SUCCESS;
            if (
                    box.getAccount().getUser().getId() != auth.getUser().getId() &&
                    !usrAccDao.isMemberShip(box.getAccount(), auth.getUser())
            ) {
                return KbErr.PERMISSION_ERROR;
            }

            box.setBlocked(true);
            KtsBox.DAO.update(box);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr deleteBox(AuthContext auth, long box_id) {
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            if (box == null) return KbErr.BOX_NOT_FOUND;
            if (box.isDisabled()) return KbErr.SUCCESS;
            if (
                    box.getAccount().getUser().getId() != auth.getUser().getId() &&
                    !usrAccDao.isMemberShip(box.getAccount(), auth.getUser())
            ) {
                return KbErr.PERMISSION_ERROR;
            }

            box.setDisabled(true);
            KtsBox.DAO.update(box);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public WithKbErr<KtsBox> getBoxByXYZ(AuthContext auth, long world_id, long x, long y, long z) {
        try {
            KtsBox box = KtsBox.DAO.queryBuilder().where()
                    .eq("world", world_id).and()
                    .eq("x", x).and()
                    .eq("y", y).and()
                    .eq("z", z).and()
                    .eq("disabled", false).queryForFirst();


            if(box == null) return new WithKbErr<>(KbErr.BOX_NOT_FOUND);
            return new WithKbErr<>(KbErr.SUCCESS, box);
        } catch (SQLException e) {
            return new WithKbErr<>(KbErr.APPLICATION_EXCEPTION);
        }
    }

    @Override
    public KbErr updateBoxData(AuthContext auth, long box_id, long now_count, Long count_per_transaction, Long buy_cost_per_transaction, Long sell_cost_per_transaction) {
        if(count_per_transaction != null && (count_per_transaction <= 0 | now_count < 0)) return KbErr.ILLEGAL_COUNT;
        if(buy_cost_per_transaction != null && buy_cost_per_transaction <= 0) return KbErr.ILLEGAL_COST;
        if(sell_cost_per_transaction != null && sell_cost_per_transaction <= 0) return KbErr.ILLEGAL_COST;
        try {
            KtsBox box = KtsBox.DAO.queryForId(box_id);
            if(box == null) return KbErr.BOX_NOT_FOUND;
            if (
                    box.getAccount().getUser().getId() != auth.getUser().getId() &&
                    !usrAccDao.isMemberShip(box.getAccount(), auth.getUser())
            ) {
                return KbErr.PERMISSION_ERROR;
            }
            box.setCountNow(now_count);
            if(count_per_transaction != null) box.setCountPerTransaction(count_per_transaction);
            if(buy_cost_per_transaction != null) box.setBuyCostPerTransaction(buy_cost_per_transaction);
            if(sell_cost_per_transaction != null) box.setSellCostPerTransaction(sell_cost_per_transaction);
            KtsBox.DAO.update(box);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr giveWarn(AuthContext auth, long user_id, long day_periods, long points, boolean banned, long fine, String message) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.give.warn")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsUser usr = usrDao.queryForId(user_id);

            KtsWarn warn = new KtsWarn();
            warn.setDoer(auth.getUser());
            warn.setGuilty(usr);
            warn.setBanned(banned);
            warn.setPunishmentPoints(points);
            warn.setMessage(message);
            warn.setFine(fine);
            warn.setBeforeTime(Timestamp.from(Instant.now().plus(day_periods, ChronoUnit.DAYS)));

            KtsWarn.DAO.create(warn);
            String msg = "Warn give notify:\n" +
                    "```diff\n" +
                    "- warn_id: " + warn.getId() + "\n" +
                    "- doer: " + auth.getUser().getNickname() + "\n" +
                    "- guilty: " + usr.getNickname() + "\n" +
                    "- pay_before: " + warn.getBeforeTime() + "\n" +
                    "- points: " + points + "\n" +
                    "- banned: " + banned + "\n" +
                    "- fine: " + fine + "\n" +
                    "- message: " + message + "\n" +
                    "```";

            dn.threadSend(usr, msg);
            if(banned) {
                usr.setBlocked(true);
                usrDao.update(usr);
            }
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }

    @Override
    public KbErr takeWarn(AuthContext auth, long warn_id) {
        if(!permissionManager.hasPermission(auth.getUser(), "ktsbk.take.warn")) {
            return KbErr.PERMISSION_ERROR;
        }
        try {
            KtsWarn warn = KtsWarn.DAO.queryForId(warn_id);
            if(warn == null) return KbErr.PERM_NOT_FOUND;
            String msg = "Warn withdrawn notify:\n" +
                    "```diff\n" +
                    "+ warn_id: " + warn.getId() + "\n" +
                    "```";
            KtsWarn.DAO.delete(warn);
            dn.threadSend(warn.getGuilty(), msg);
            return KbErr.SUCCESS;
        } catch (SQLException e) {
            return KbErr.APPLICATION_EXCEPTION;
        }
    }
}
