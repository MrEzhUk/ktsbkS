package kts.dev.ktsbk.server.auth;

import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.utils.AuthContext;
import kts.dev.ktsbk.common.utils.KbErr;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    public static final AuthManager INSTANCE = new AuthManager();
    private final KtsUserDao usrDao = KtsDbSource.INSTANCE.createDao(KtsUser.class);
    private AuthManager() {}
    public KtsUser retrieveUser(String token) {
        KtsUser user;

        try {
            List<KtsUser> users = usrDao.queryForEq("token", token);
            if(users.isEmpty()) return null;
            user = users.get(0);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
