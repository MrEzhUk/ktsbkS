package kts.dev.ktsbk.common.services;

import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.box.KtsBoxType;
import kts.dev.ktsbk.common.db.currencies.KtsCurrency;
import kts.dev.ktsbk.common.db.multiworld.KtsServer;
import kts.dev.ktsbk.common.db.multiworld.KtsWorld;
import kts.dev.ktsbk.common.db.users.KtsPermission;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.utils.*;

import java.net.InetAddress;
import java.util.List;


@SuppressWarnings("unused")
public interface KtsBkServiceC2S {
    // Users
    KbErr createUser(AuthContext auth, String nickname, String disid);
    KbErr resetPassword(AuthContext auth, long userId);
    KbErr disableUser(AuthContext auth, long userId);
    KbErr restoreUser(AuthContext auth, long userId);
    KbErr blockUser(AuthContext auth, long userId);
    KbErr unblockUser(AuthContext auth, long userId);
    WithKbErr<List<KtsUser>> autoCompleteUser(AuthContext auth, String end);
    WithKbErr<KtsUser> getUserById(AuthContext auth, long userId);
    // Permission
    WithKbErr<List<KtsPermission>> seeUserPermissions(AuthContext auth, long userId);
    KbErr givePermission(AuthContext auth, long userId, long permissionId);
    KbErr takePermission(AuthContext auth, long userId, long permissionId);
    WithKbErr<List<KtsPermission>> autoCompletePermission(AuthContext auth, String end);
    // Servers
    KbErr createServer(AuthContext auth, InetAddress ip, String name, String shortName);
    KbErr blockServer(AuthContext auth, long serverId);
    KbErr unblockServer(AuthContext auth, long serverId);
    WithKbErr<List<KtsServer>> autoCompleteServer(AuthContext auth, String end);
    // Worlds
    KbErr createWorld(AuthContext auth, long serverId, String mcName, String ktsbkName);
    WithKbErr<KtsWorld> getWorldById(AuthContext auth, long worldId);
    WithKbErr<KtsWorld> getWorldByKtsBkName(AuthContext auth, String ktsbk_string);
    WithKbErr<List<KtsWorld>> autoCompleteWorld(AuthContext auth, String end);
    // Currencies
    KbErr createCurrency(AuthContext auth, long serverId, String name, String short_name);
    KbErr setCurrencyTransactionPercent(AuthContext auth, long currencyId, long percent);
    KbErr setCurrencyBoxRent(AuthContext auth, long currencyId, long rent);
    WithKbErr<List<KtsCurrency>> autoCompleteCurrency(AuthContext auth, String end);
    // Accounts
    KbErr createAccount(AuthContext auth, String name, long currencyId);
    KbErr blockAccount(AuthContext auth, long accountId);
    KbErr unblockAccount(AuthContext auth, long accountId);
    KbErr disableAccount(AuthContext auth, long accountId);
    KbErr restoreAccount(AuthContext auth, long accountId);
    KbErr holdAccountMoney(AuthContext auth, long accountId, long count);
    WithKbErr<List<KtsAccount>> autoCompleteAccount(AuthContext auth, String end);
    WithKbErr<KtsAccount> getAccountById(AuthContext auth, long accountId);
    WithKbErr<List<KtsAccount>> getMyAccounts(AuthContext auth);
    WithKbErr<List<KtsAccount>> getMyMembership(AuthContext auth);
    WithKbErr<List<KtsUser>> getAccountMembership(AuthContext auth, long accountId);
    KbErr pay(AuthContext auth, long accountId1, long accountId2, long count, String msg);
    KbErr kick(AuthContext auth, long accountId, long userId);
    KbErr invite(AuthContext auth, long accountId, long userId);
    KbErr io_money(AuthContext auth, long accountId, long count);
    // BSideBox
    KbErr createBox(
            AuthContext auth,
            long x, long y, long z, long worldId,
            long accountId,
            String itemIdentifier, String itemSerializable,
            long count_per_slot, long buy_cost_per_slot, long sell_cost_per_slot,
            KtsBoxType type
    );
    WithKbErr<List<KtsBox>> getMyBoxes(AuthContext auth);
    KbErr buyInBox(AuthContext auth, long account_id, long box_id, long count_transactions);
    KbErr sellInBox(AuthContext auth, long account_id, long box_id, long count_transactions);
    WithKbErr<List<KtsBox>> searchBox(AuthContext auth, SearchProduct criteria);
    WithKbErr<KtsBox> getBoxId(AuthContext auth, long box_id);
    KbErr unblockBox(AuthContext auth, long box_id);
    KbErr blockBox(AuthContext auth, long box_id);
    KbErr deleteBox(AuthContext auth, long box_id);
    WithKbErr<KtsBox> getBoxByXYZ(AuthContext auth, long world_id, long x, long y, long z);
    KbErr updateBoxData(AuthContext auth, long box_id, long now_count, Long count_per_transaction, Long buy_cost_per_transaction, Long sell_cost_per_transaction);
    KbErr giveWarn(AuthContext auth, long user_id, long day_periods, long points, boolean banned, long fine, String message);
    KbErr takeWarn(AuthContext auth, long warn_id);
}
