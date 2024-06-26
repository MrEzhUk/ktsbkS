package kts.dev.ktsbk.common.utils;

public class Pair<T, U>{
    public final T t;
    public final U u;
    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }
    public Pair(T t) {
        this(t, null);
    }

    @Override
    public String toString() {
        return (t != null ? t.toString() : "(null)") + " " + (u != null ? u.toString() : "(null)");
    }
}