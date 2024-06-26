package kts.dev.ktsbk.common.utils;

public class WithKbErr<T> {
    public KbErr t;
    public T u;

    public WithKbErr(KbErr t, T u) {
        this.t = t;
        this.u = u;
    }

    public WithKbErr(KbErr t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return (t != null ? t.toString() : "(null)") + " " + (u != null ? u.toString() : "(null)");
    }
}
