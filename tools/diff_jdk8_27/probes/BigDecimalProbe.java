import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.TimeZone;

public class BigDecimalProbe {
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
            emit("add", new BigDecimal("0.1").add(new BigDecimal("0.2")).toString());
            emit("subtract", new BigDecimal("1.0").subtract(new BigDecimal("0.9")).toString());
            emit("multiply", new BigDecimal("1.1").multiply(new BigDecimal("1.1")).toString());
            emit("scale", new BigDecimal("1.2300").scale());
            emit("stripTrailingZeros", new BigDecimal("1.2300").stripTrailingZeros().toString());
            emit("setScale.HALF_UP", new BigDecimal("1.235").setScale(2, RoundingMode.HALF_UP).toString());
            emit("setScale.HALF_DOWN", new BigDecimal("1.235").setScale(2, RoundingMode.HALF_DOWN).toString());
            emit("setScale.HALF_EVEN", new BigDecimal("1.235").setScale(2, RoundingMode.HALF_EVEN).toString());
            emit("setScale.HALF_EVEN2", new BigDecimal("1.225").setScale(2, RoundingMode.HALF_EVEN).toString());
            emit("setScale.CEILING.neg", new BigDecimal("-1.21").setScale(1, RoundingMode.CEILING).toString());
            emit("setScale.FLOOR.neg", new BigDecimal("-1.29").setScale(1, RoundingMode.FLOOR).toString());
            emit("setScale.UP", new BigDecimal("1.201").setScale(2, RoundingMode.UP).toString());
            emit("divide", new BigDecimal("1").divide(new BigDecimal("3"), 5, RoundingMode.HALF_UP).toString());
            ex("divide.noScale", () -> emit("divide.noScale", new BigDecimal("1").divide(new BigDecimal("3"))));
            emit("compareTo.diffScale", new BigDecimal("1.0").compareTo(new BigDecimal("1.00")));
            emit("equals.diffScale", new BigDecimal("1.0").equals(new BigDecimal("1.00")));
            emit("negate", new BigDecimal("-1.5").negate().toString());
            emit("abs", new BigDecimal("-1.5").abs().toString());
            emit("pow", new BigDecimal("2").pow(10).toString());
            emit("valueOf.0.1", BigDecimal.valueOf(0.1).toString());
            emit("ctor.0.1", new BigDecimal(0.1).toString());
            emit("movePointLeft", new BigDecimal("1234.5").movePointLeft(2).toString());
            emit("movePointRight", new BigDecimal("1.2345").movePointRight(2).toString());
            ex("divideByZero", () -> emit("divideByZero", new BigDecimal("1").divide(new BigDecimal("0"), RoundingMode.HALF_UP)));
            emit("scaleNegative", new BigDecimal("1E+2").scale());
            emit("precision", new BigDecimal("1.2300").precision());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
