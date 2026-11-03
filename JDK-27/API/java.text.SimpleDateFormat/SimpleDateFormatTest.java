/*
 * @test
 * @summary java.text.SimpleDateFormat conformance: white-box structure + black-box pattern/format/parse behavior.
 * @run main SimpleDateFormatTest
 */

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleDateFormatTest {
    static int passed;
    static final Locale US = Locale.US;
    static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    static final Date EPOCH = new Date(0L);

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(ThrowingRunnable r, Class<? extends Throwable> t, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (t.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " but got " + x, x);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName());
    }

    static String fmt(String pattern) {
        SimpleDateFormat s = new SimpleDateFormat(pattern, US);
        s.setTimeZone(UTC);
        return s.format(EPOCH);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.SimpleDateFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.text.DateFormat"), "extends DateFormat");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Locale.class) != null, "ctor(String,Locale)");
        check(c.getConstructor(String.class, DateFormatSymbols.class) != null, "ctor(String,DateFormatSymbols)");
        check(c.getMethod("toPattern") != null && c.getMethod("applyPattern", String.class) != null, "pattern methods");
        check(c.getMethod("formatToCharacterIterator", Object.class) != null, "formatToCharacterIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPatternLetters() {
        check("1970".equals(fmt("yyyy")), "yyyy -> 1970");
        check("01".equals(fmt("MM")), "MM -> 01");
        check("01".equals(fmt("dd")), "dd -> 01");
        check("00".equals(fmt("HH")), "HH -> 00");
        check("00".equals(fmt("mm")), "mm -> 00");
        check("00".equals(fmt("ss")), "ss -> 00");
        check("000".equals(fmt("SSS")), "SSS -> 000");
        check("Thu".equals(fmt("EEE")), "EEE -> Thu");
        check("Thursday".equals(fmt("EEEE")), "EEEE -> Thursday");
        check("Jan".equals(fmt("MMM")), "MMM -> Jan");
        check("January".equals(fmt("MMMM")), "MMMM -> January");
        check("AD".equals(fmt("G")), "G -> AD");
        check("AM".equals(fmt("a")), "a -> AM");
        check("UTC".equals(fmt("z")), "z -> UTC");
        check("1970-01-01".equals(fmt("yyyy-MM-dd")), "yyyy-MM-dd");
        check("1970/01/01 00:00:00.000".equals(fmt("yyyy/MM/dd HH:mm:ss.SSS")), "full timestamp");
        check("12:00 AM".equals(fmt("hh:mm a")), "12-hour with AM");
        check("Thu, 1 Jan 1970 00:00:00 UTC".equals(fmt("EEE, d MMM yyyy HH:mm:ss z")), "RFC-ish pattern");
    }

    static void testRoundTrip() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", US);
        sdf.setTimeZone(UTC);
        String s = sdf.format(EPOCH);
        check("1970-01-01 00:00:00.000".equals(s), "format epoch");
        Date back = sdf.parse(s);
        check(back.getTime() == 0L, "parse round-trip -> epoch 0");

        SimpleDateFormat lenient = new SimpleDateFormat("yyyy/MM/dd", US);
        lenient.setTimeZone(UTC);
        Date d = lenient.parse("1970/06/15");
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd", US);
        day.setTimeZone(UTC);
        check("1970-06-15".equals(day.format(d)), "parse arbitrary date");

        expectThrows(() -> sdf.parse("not-a-date"), ParseException.class, "parse garbage throws ParseException");
    }

    static void testTwoDigitYear() throws Exception {
        SimpleDateFormat yy = new SimpleDateFormat("yy-MM-dd", US);
        yy.setTimeZone(UTC);
        yy.set2DigitYearStart(new Date(0L));
        check("70-01-01".equals(yy.format(EPOCH)), "yy format -> 70-01-01");
        check(yy.get2DigitYearStart().getTime() == 0L, "get2DigitYearStart");
        check(yy.parse("70-01-01").getTime() == 0L, "yy parse -> epoch 0");
    }

    static void testPatternMutation() {
        SimpleDateFormat s = new SimpleDateFormat("yyyy", US);
        check("yyyy".equals(s.toPattern()), "toPattern");
        check("yyyy".equals(s.toLocalizedPattern()), "toLocalizedPattern US == pattern");
        s.applyPattern("MM/dd/yyyy");
        check("MM/dd/yyyy".equals(s.toPattern()), "applyPattern changes toPattern");
        s.setTimeZone(UTC);
        check("01/01/1970".equals(s.format(EPOCH)), "applied pattern format");
        s.applyLocalizedPattern("yyyy-MM-dd");
        check("yyyy-MM-dd".equals(s.toPattern()), "applyLocalizedPattern changes pattern");
        check(s.toString().contains("yyyy-MM-dd"), "toString contains pattern");
    }

    static void testCharacterIteratorAndSymbols() {
        SimpleDateFormat s = new SimpleDateFormat("yyyy", US);
        s.setTimeZone(UTC);
        AttributedCharacterIterator it = s.formatToCharacterIterator(EPOCH);
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("1970".equals(sb.toString()), "formatToCharacterIterator text");

        DateFormatSymbols custom = DateFormatSymbols.getInstance(US);
        custom.setMonths(new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M"});
        SimpleDateFormat m = new SimpleDateFormat("MMMM", US);
        m.setTimeZone(UTC);
        m.setDateFormatSymbols(custom);
        check("A".equals(m.format(EPOCH)), "setDateFormatSymbols custom month name");
        check(custom.equals(m.getDateFormatSymbols()), "getDateFormatSymbols returns equal symbols");
    }

    static void testEqualsCloneHashCode() {
        SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd", US);
        a.setTimeZone(UTC);
        SimpleDateFormat b = new SimpleDateFormat("yyyy-MM-dd", US);
        b.setTimeZone(UTC);
        check(a.equals(b) && b.equals(a), "equal pattern+tz equal");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        SimpleDateFormat c = (SimpleDateFormat) a.clone();
        check(a.equals(c) && c.equals(a), "clone equals");
        check(a.hashCode() == c.hashCode(), "clone hashCode");
        SimpleDateFormat d = new SimpleDateFormat("yyyy/MM/dd", US);
        d.setTimeZone(UTC);
        check(!a.equals(d), "different pattern not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPatternLetters();
        testRoundTrip();
        testTwoDigitYear();
        testPatternMutation();
        testCharacterIteratorAndSymbols();
        testEqualsCloneHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
