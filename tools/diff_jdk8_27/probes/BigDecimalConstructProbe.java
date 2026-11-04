import java.math.BigDecimal;
import java.util.Locale;
import java.util.TimeZone;

public class BigDecimalConstructProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=exception=" + t.getClass().getName()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            ex("ctor.null", () -> emit("ctor.null", new BigDecimal((String) null)));
            ex("ctor.empty", () -> emit("ctor.empty", new BigDecimal("")));
            emit("ctor.plus", new BigDecimal("+1.5").toString());
            emit("ctor.leadingZero", new BigDecimal("0001.5000").toString());
            emit("ctor.leadingZero.scale", new BigDecimal("0001.5000").scale());
            emit("ctor.exp", new BigDecimal("1e3").toString());
            emit("ctor.expPlus", new BigDecimal("1E+3").toString());
            emit("ctor.expNeg", new BigDecimal("1.5e-2").toString());
            ex("ctor.badExp", () -> emit("ctor.badExp", new BigDecimal("1e")));
            ex("ctor.badExp2", () -> emit("ctor.badExp2", new BigDecimal("1e+")));
            emit("ctor.negZero", new BigDecimal("-0.0").toString());
            emit("ctor.negZero.scale", new BigDecimal("-0.00").scale());
            emit("ctor.expZero", new BigDecimal("0E+5").toString());
            ex("ctor.onlyPlus", () -> emit("ctor.onlyPlus", new BigDecimal("+")));
            ex("ctor.onlyMinus", () -> emit("ctor.onlyMinus", new BigDecimal("-")));
            emit("ctor.double.0.1", new BigDecimal(0.1).toString());
            emit("ctor.double.1.0", new BigDecimal(1.0).toString());
            emit("ctor.long.min", new BigDecimal(Long.MIN_VALUE).toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
