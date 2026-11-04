import java.util.Locale;
import java.util.TimeZone;

public class DoubleParseProbe {
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
            ex("parse.MAX", () -> emit("parse.MAX", Double.parseDouble("1.7976931348623157E308")));
            ex("parse.minSubnormal", () -> emit("parse.minSubnormal", Double.parseDouble("4.9E-324")));
            ex("parse.overflow", () -> emit("parse.overflow", Double.parseDouble("1e309")));
            ex("parse.underflow", () -> emit("parse.underflow", Double.parseDouble("1e-400")));
            ex("parse.hex", () -> emit("parse.hex", Double.parseDouble("0x1.0p0")));
            ex("parse.hexNeg", () -> emit("parse.hexNeg", Double.parseDouble("-0x1.8p1")));
            ex("parse.nan", () -> emit("parse.nan", Double.parseDouble("NaN")));
            ex("parse.inf", () -> emit("parse.inf", Double.parseDouble("Infinity")));
            ex("parse.negInf", () -> emit("parse.negInf", Double.parseDouble("-Infinity")));
            ex("parse.negZero", () -> emit("parse.negZero", Double.parseDouble("-0.0")));
            ex("parse.dSuffix", () -> emit("parse.dSuffix", Double.parseDouble("1d")));
            ex("parse.fSuffix", () -> emit("parse.fSuffix", Double.parseDouble("1f")));
            ex("parse.empty", () -> emit("parse.empty", Double.parseDouble("")));
            ex("parse.null", () -> emit("parse.null", Double.parseDouble(null)));
            emit("isNaN", Double.isNaN(Double.parseDouble("NaN")));
            emit("bitNegZero", Double.doubleToLongBits(-0.0));
            emit("bitPosZero", Double.doubleToLongBits(0.0));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
