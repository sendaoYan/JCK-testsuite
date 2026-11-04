import java.util.Locale;
import java.util.TimeZone;

public class DoubleToStringProbe {
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
            double[] vals = {
                0.1, 0.2, 0.3, 1.0 / 3.0, 1e23, 1e-23,
                2.2250738585072014E-308, 4.9E-324, 1.7976931348623157E308,
                Math.PI, Math.E, 0.5, 2.0, 1000000.0, 999999.9999999999,
                1.005, 123.45678901234567, 3.141592653589793, -0.0, 5.0e-10
            };
            for (int i = 0; i < vals.length; i++) {
                emit("toString." + i, Double.toString(vals[i]));
                emit("toHex." + i, Double.toHexString(vals[i]));
            }
            emit("toString.nan", Double.toString(Double.NaN));
            emit("toString.posInf", Double.toString(Double.POSITIVE_INFINITY));
            emit("toString.negInf", Double.toString(Double.NEGATIVE_INFINITY));
            emit("toString.minValue", Double.toString(Double.MIN_VALUE));
            emit("toString.minNormal", Double.toString(Double.MIN_NORMAL));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
