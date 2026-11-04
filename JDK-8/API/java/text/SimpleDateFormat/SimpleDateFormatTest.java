/*
 * @test
 * @summary java.text.SimpleDateFormat API conformance: full contract coverage.
 *          constructors, deterministic pattern letters under fixed Locale.US + UTC,
 *          format/parse round-trip, ParseException on garbage, 2-digit-year window,
 *          applyPattern/applyLocalizedPattern/toPattern/toLocalizedPattern,
 *          formatToCharacterIterator, setDateFormatSymbols, equals/hashCode/clone,
 *          lenient strict mode, ParsePosition advancement, and serialization round-trip.
 * @run main SimpleDateFormatTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            if (x instanceof AssertionError) throw (AssertionError) x;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
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
        check("java.text.DateFormat".equals(c.getSuperclass().getName()), "extends DateFormat");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Locale.class) != null, "ctor(String,Locale)");
        check(c.getConstructor(String.class, DateFormatSymbols.class) != null,
              "ctor(String,DateFormatSymbols)");
        check(c.getMethod("toPattern") != null, "toPattern()");
        check(c.getMethod("applyPattern", String.class) != null, "applyPattern(String)");
        check(c.getMethod("formatToCharacterIterator", Object.class) != null,
              "formatToCharacterIterator(Object)");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
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
        check("Thu, 1 Jan 1970 00:00:00 UTC".equals(fmt("EEE, d MMM yyyy HH:mm:ss z")),
              "RFC-ish pattern");
    }

    static void testConstructorsAndExceptions() {
        // bad pattern -> IllegalArgumentException (unterminated quote)
        expectThrows(IllegalArgumentException.class, () -> new SimpleDateFormat("'"),
                     "unterminated quote rejected");
        expectThrows(NullPointerException.class, () -> new SimpleDateFormat(null),
                     "ctor(null) -> NPE");
        expectThrows(NullPointerException.class, () -> new SimpleDateFormat("yyyy", (Locale) null),
                     "ctor(String, null Locale) -> NPE");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", US);
        sdf.setTimeZone(UTC);
        expectThrows(NullPointerException.class, () -> sdf.format((Date) null),
                     "format((Date) null) -> NPE");
        expectThrows(NullPointerException.class, () -> sdf.parse((String) null),
                     "parse((String) null) -> NPE");
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

        expectThrows(ParseException.class, () -> sdf.parse("not-a-date"),
                     "parse garbage throws ParseException");
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
        check(s.toString() != null, "toString non-null (JDK-8 inherits Object.toString)");
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

    static void testLenientStrictAndParsePosition() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(UTC, US);
        cal.clear();
        cal.set(2000, Calendar.FEBRUARY, 29, 0, 0, 0);
        Date leap = cal.getTime();
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd", US);
        ymd.setTimeZone(UTC);
        check("2000-02-29".equals(ymd.format(leap)), "leap day format");
        ParsePosition pLeap = new ParsePosition(0);
        check(ymd.parse("2000-02-29", pLeap).getTime() == leap.getTime(), "leap day parse round-trip");

        SimpleDateFormat strict = new SimpleDateFormat("yyyy-MM-dd", US);
        strict.setTimeZone(UTC);
        strict.setLenient(false);
        check(!strict.isLenient(), "setLenient(false)");
        ParsePosition pStrict = new ParsePosition(0);
        check(strict.parse("2000-01-01", pStrict).getTime()
                      == ymd.parse("2000-01-01", new ParsePosition(0)).getTime(),
              "strict parse valid date");

        ParsePosition pp = new ParsePosition(0);
        Date pd = ymd.parse("1970-01-01", pp);
        check(pd != null && pd.getTime() == 0L, "parse(String,ParsePosition) result");
        check(pp.getIndex() == "1970-01-01".length(), "parse position advanced fully");

        AttributedCharacterIterator it2 = ymd.formatToCharacterIterator(EPOCH);
        check(it2.getEndIndex() == "1970-01-01".length(), "formatToCharacterIterator end index");

        // non-lenient rejects out-of-range month
        ParsePosition pBad = new ParsePosition(0);
        check(strict.parse("2000-13-01", pBad) == null, "strict parse invalid month -> null");
        check(pBad.getErrorIndex() >= 0, "strict parse invalid month sets error index");
    }

    static void testSymbolsCtorAndNoArg() {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(US);
        SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd", dfs);
        s1.setTimeZone(UTC);
        check("1970-01-01".equals(s1.format(EPOCH)), "ctor(String,DateFormatSymbols) format");
        check(dfs.equals(s1.getDateFormatSymbols()), "ctor(String,DateFormatSymbols) symbols round-trip");

        SimpleDateFormat s2 = new SimpleDateFormat();
        s2.setTimeZone(UTC);
        String out = s2.format(EPOCH);
        check(out != null && out.length() > 0, "no-arg ctor formats non-empty");
        check(s2.toPattern() != null && s2.toPattern().length() > 0, "no-arg ctor has a pattern");
    }

    static void testSerialization() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", US);
        sdf.setTimeZone(UTC);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(sdf);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        SimpleDateFormat r = (SimpleDateFormat) ois.readObject();
        check("yyyy-MM-dd HH:mm:ss".equals(r.toPattern()), "round-trip preserves pattern");
        check("1970-01-01 00:00:00".equals(r.format(EPOCH)), "round-trip still formats");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPatternLetters();
        testConstructorsAndExceptions();
        testRoundTrip();
        testTwoDigitYear();
        testPatternMutation();
        testCharacterIteratorAndSymbols();
        testEqualsCloneHashCode();
        testLenientStrictAndParsePosition();
        testSymbolsCtorAndNoArg();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
