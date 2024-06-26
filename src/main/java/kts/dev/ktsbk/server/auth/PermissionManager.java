package kts.dev.ktsbk.server.auth;

import com.j256.ormlite.stmt.QueryBuilder;
import kts.dev.ktsbk.common.db.users.KtsPermission;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.db.bridge.KtsUserPermission;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;

import java.sql.SQLException;
import java.util.List;

public class PermissionManager {
    public PermissionManager() {}
    private final KtsUserDao usrDao = KtsDbSource.INSTANCE.createDao(KtsUser.class);
    public KtsPermission addPermission(String permissionString) {
        try {
            List<KtsPermission> perms = KtsPermission.DAO.queryForEq("permission_string", permissionString);
            if(!perms.isEmpty()) return perms.get(0);
            KtsPermission perm = new KtsPermission();
            perm.setPermissionString(permissionString);
            KtsPermission.DAO.create(perm);
            KtsPermission.DAO.refresh(perm);
            return perm;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean hasPermission(KtsUser usr, String permissionString) {
        try {
            QueryBuilder<KtsUser, Long> j_usr = usrDao.queryBuilder();
            j_usr.where().eq("id", usr.getId());
            QueryBuilder<KtsPermission, Long> j_perm = KtsPermission.DAO.queryBuilder();
            j_perm.where().eq("permission_string", permissionString);
            return KtsUserPermission.DAO.queryBuilder().join(j_usr).join(j_perm).queryForFirst() != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkCurrencyPermission(KtsUser usr, long currencyId, String permissionName) {
        return hasPermission(usr, "currency." + currencyId + "." + permissionName);
    }
}
