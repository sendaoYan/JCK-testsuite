import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DecimalFormatProbe {
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
            DecimalFormat df = new DecimalFormat("#,##0.00");
            emit("group", df.format(1234567.891));
            df.applyPattern("0000.000");
            emit("pad", df.format(12.5));
            df.applyPattern("#.##E0");
            emit("sci", df.format(12345.678));
            df.applyPattern("0.###");
            emit("round", df.format(0.12345));

            DecimalFormat df2 = new DecimalFormat("0.00");
            df2.setRoundingMode(RoundingMode.HALF_EVEN);
            emit("halfEven.1.235", df2.format(1.235));
            emit("halfEven.1.225", df2.format(1.225));

            DecimalFormat df3 = new DecimalFormat("0.00");
            df3.setRoundingMode(RoundingMode.HALF_UP);
            emit("halfUp.1.235", df3.format(1.235));

            emit("negParen", new DecimalFormat("#,##0;(#,##0)").format(-1234));
            emit("posParen", new DecimalFormat("#,##0;(#,##0)").format(1234));
            emit("percentPattern", new DecimalFormat("0.##%").format(0.125));
            emit("multiplier", new DecimalFormat("0.###").format(3.0));

            DecimalFormat parseFmt = new DecimalFormat("#,##0.##");
            emit("parse", parseFmt.parse("1,234.56").toString());
            ex("parse.garbage", () -> emit("parse.garbage", parseFmt.parse("abc")));

            emit("toPattern", new DecimalFormat("0000.00").toPattern());
            ex("applyPattern.bad", () -> new DecimalFormat().applyPattern("##"));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
