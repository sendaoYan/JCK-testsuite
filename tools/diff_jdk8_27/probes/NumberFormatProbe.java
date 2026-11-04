import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

public class NumberFormatProbe {
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
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            emit("int", nf.format(1234567));
            emit("double", nf.format(1234.567));
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(0);
            emit("maxFrac", nf.format(1234.567));
            nf.setMinimumFractionDigits(3);
            emit("minFrac", nf.format(1234.5));
            nf.setGroupingUsed(false);
            emit("noGroup", nf.format(1234567));

            NumberFormat pf = NumberFormat.getPercentInstance(Locale.US);
            emit("percent", pf.format(0.125));
            emit("percent.1", pf.format(1));

            NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.US);
            emit("currency", cf.format(1234.56));
            emit("currency.neg", cf.format(-1234.56));

            NumberFormat iff = NumberFormat.getIntegerInstance(Locale.US);
            emit("integer", iff.format(1234.56));

            NumberFormat de = NumberFormat.getNumberInstance(Locale.GERMANY);
            emit("german", de.format(1234567.89));

            NumberFormat fr = NumberFormat.getNumberInstance(Locale.FRANCE);
            emit("french", fr.format(1234567.89));

            NumberFormat nfParse = NumberFormat.getNumberInstance(Locale.US);
            nfParse.setParseIntegerOnly(true);
            emit("parseIntOnly", nfParse.parse("1234.5").toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
