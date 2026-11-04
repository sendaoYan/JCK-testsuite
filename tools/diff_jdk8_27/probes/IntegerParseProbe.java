import java.util.Locale;
import java.util.TimeZone;

public class IntegerParseProbe {
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
            ex("parse.MAX", () -> emit("parse.MAX", Integer.parseInt("2147483647")));
            ex("parse.MIN", () -> emit("parse.MIN", Integer.parseInt("-2147483648")));
            ex("parse.MAXp1", () -> emit("parse.MAXp1", Integer.parseInt("2147483648")));
            ex("parse.MINm1", () -> emit("parse.MINm1", Integer.parseInt("-2147483649")));
            ex("parse.plus", () -> emit("parse.plus", Integer.parseInt("+42")));
            ex("parse.minus", () -> emit("parse.minus", Integer.parseInt("-42")));
            ex("parse.empty", () -> emit("parse.empty", Integer.parseInt("")));
            ex("parse.spaceLead", () -> emit("parse.spaceLead", Integer.parseInt(" 42")));
            ex("parse.spaceTrail", () -> emit("parse.spaceTrail", Integer.parseInt("42 ")));
            ex("parse.radix36", () -> emit("parse.radix36", Integer.parseInt("z", 36)));
            ex("parse.radix37", () -> emit("parse.radix37", Integer.parseInt("1", 37)));
            ex("parse.radix1", () -> emit("parse.radix1", Integer.parseInt("1", 1)));
            ex("parse.hexNegMin", () -> emit("parse.hexNegMin", Integer.parseInt("-80000000", 16)));
            ex("parse.hexPosMax", () -> emit("parse.hexPosMax", Integer.parseInt("7fffffff", 16)));
            ex("parse.hexOverflow", () -> emit("parse.hexOverflow", Integer.parseInt("80000000", 16)));
            ex("parse.arabicDigit", () -> emit("parse.arabicDigit", Integer.parseInt("١")));
            ex("decode.hex", () -> emit("decode.hex", Integer.decode("0x10")));
            ex("decode.octal", () -> emit("decode.octal", Integer.decode("010")));
            ex("decode.negHex", () -> emit("decode.negHex", Integer.decode("-0x10")));
            emit("valueOf.cache127", Integer.valueOf(127) == Integer.valueOf(127));
            emit("valueOf.cache128", Integer.valueOf(128) == Integer.valueOf(128));
            emit("toString.MIN", Integer.toString(Integer.MIN_VALUE));
            emit("toHexString.MIN", Integer.toHexString(Integer.MIN_VALUE));
            emit("toOctalString.MIN", Integer.toOctalString(Integer.MIN_VALUE));
            emit("toString.radix36.MAX", Integer.toString(Integer.MAX_VALUE, 36));
            ex("parse.null", () -> emit("parse.null", Integer.parseInt(null)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
