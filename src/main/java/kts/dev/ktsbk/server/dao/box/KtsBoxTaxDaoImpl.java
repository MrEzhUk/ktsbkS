package kts.dev.ktsbk.server.dao.box;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import kts.dev.ktsbk.common.db.box.KtsBoxTax;

import java.sql.SQLException;

public class KtsBoxTaxDaoImpl extends BaseDaoImpl<KtsBoxTax, Long> implements KtsBoxTaxDao {
    public KtsBoxTaxDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, KtsBoxTax.class);
    }
}
