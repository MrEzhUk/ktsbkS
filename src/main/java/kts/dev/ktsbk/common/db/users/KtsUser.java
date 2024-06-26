package kts.dev.ktsbk.common.db.users;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kts.dev.ktsbk.server.dao.users.KtsUserDaoImpl;

import java.io.Serializable;

@DatabaseTable(tableName = "users", daoClass = KtsUserDaoImpl.class)
public class KtsUser implements Serializable {
    @DatabaseField(columnName = "id", generatedId = true)
    long id;
    @DatabaseField(columnName = "disid")
    String disId;
    @DatabaseField(columnName = "nickname")
    String nickname;
    @DatabaseField(columnName = "token")
    transient String token;
    @DatabaseField(columnName = "blocked")
    boolean blocked = false;
    @DatabaseField(columnName = "disabled")
    boolean disabled = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisId() {
        return disId;
    }

    public void setDisId(String disId) {
        this.disId = disId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        return "KtsUser{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", blocked=" + blocked +
                ", disabled=" + disabled +
                '}';
    }
}
