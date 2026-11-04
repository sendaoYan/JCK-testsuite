import java.util.Locale;
import java.util.TimeZone;

public class MathProbe {
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
            emit("floor.neg", Math.floor(-1.5));
            emit("ceil.neg", Math.ceil(-1.5));
            emit("round.negHalf", Math.round(-1.5));
            emit("round.posHalf", Math.round(1.5));
            emit("round.negHalf2", Math.round(-2.5));
            emit("round.longMax", Math.round((double) Long.MAX_VALUE));
            emit("min.nan", Double.toString(Math.min(Double.NaN, 1.0)));
            emit("max.nan", Double.toString(Math.max(Double.NaN, 1.0)));
            emit("min.negZero", Double.toString(Math.min(0.0, -0.0)));
            emit("max.negZero", Double.toString(Math.max(0.0, -0.0)));
            emit("copySign", Double.toString(Math.copySign(1.0, -0.0)));
            emit("ulp.0", Double.toString(Math.ulp(0.0)));
            emit("ulp.max", Double.toString(Math.ulp(Double.MAX_VALUE)));
            emit("nextUp.0", Double.toString(Math.nextUp(0.0)));
            emit("nextDown.0", Double.toString(Math.nextDown(0.0)));
            emit("IEEEremainder", Double.toString(Math.IEEEremainder(5.0, 2.0)));
            emit("abs.min", Math.abs(Integer.MIN_VALUE));
            emit("abs.doubleNeg", Double.toString(Math.abs(-3.14)));
            emit("pow.2.10", Math.pow(2.0, 10.0));
            emit("sqrt.neg", Double.toString(Math.sqrt(-1.0)));
            emit("scalb", Double.toString(Math.scalb(1.0, 10)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
