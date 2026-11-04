/*
 * @test
 * @summary java.text.DateFormat conformance: white-box structure + black-box factory/format/parse behavior.
 * @run main DateFormatTest
 */

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.DateFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.text.Format"), "extends Format");

        check(c.getField("ERA_FIELD").getInt(null) == 0, "ERA_FIELD=0");
        check(c.getField("YEAR_FIELD").getInt(null) == 1, "YEAR_FIELD=1");
        check(c.getField("MONTH_FIELD").getInt(null) == 2, "MONTH_FIELD=2");
        check(c.getField("DATE_FIELD").getInt(null) == 3, "DATE_FIELD=3");
        check(c.getField("HOUR_OF_DAY1_FIELD").getInt(null) == 4, "HOUR_OF_DAY1_FIELD=4");
        check(c.getField("HOUR_OF_DAY0_FIELD").getInt(null) == 5, "HOUR_OF_DAY0_FIELD=5");
        check(c.getField("MINUTE_FIELD").getInt(null) == 6, "MINUTE_FIELD=6");
        check(c.getField("SECOND_FIELD").getInt(null) == 7, "SECOND_FIELD=7");
        check(c.getField("MILLISECOND_FIELD").getInt(null) == 8, "MILLISECOND_FIELD=8");
        check(c.getField("DAY_OF_WEEK_FIELD").getInt(null) == 9, "DAY_OF_WEEK_FIELD=9");
        check(c.getField("AM_PM_FIELD").getInt(null) == 14, "AM_PM_FIELD=14");
        check(c.getField("TIMEZONE_FIELD").getInt(null) == 17, "TIMEZONE_FIELD=17");
        check(c.getField("FULL").getInt(null) == 0, "FULL=0");
        check(c.getField("LONG").getInt(null) == 1, "LONG=1");
        check(c.getField("MEDIUM").getInt(null) == 2, "MEDIUM=2");
        check(c.getField("SHORT").getInt(null) == 3, "SHORT=3");
        check(c.getField("DEFAULT").getInt(null) == 2, "DEFAULT=2");

        check(DateFormat.getDateInstance() instanceof DateFormat, "getDateInstance() non-null");
        check(DateFormat.getTimeInstance() instanceof DateFormat, "getTimeInstance() non-null");
        check(DateFormat.getDateTimeInstance() instanceof DateFormat, "getDateTimeInstance() non-null");
        check(DateFormat.getInstance() instanceof DateFormat, "getInstance() non-null");
        check(DateFormat.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testFactoryFormatStrings() {
        DateFormat d;
        d = DateFormat.getDateInstance(DateFormat.SHORT, US); d.setTimeZone(UTC);
        check("1/1/70".equals(d.format(EPOCH)), "date SHORT -> 1/1/70");
        d = DateFormat.getDateInstance(DateFormat.MEDIUM, US); d.setTimeZone(UTC);
        check("Jan 1, 1970".equals(d.format(EPOCH)), "date MEDIUM -> Jan 1, 1970");
        d = DateFormat.getDateInstance(DateFormat.LONG, US); d.setTimeZone(UTC);
        check("January 1, 1970".equals(d.format(EPOCH)), "date LONG -> January 1, 1970");
        d = DateFormat.getDateInstance(DateFormat.FULL, US); d.setTimeZone(UTC);
        check("Thursday, January 1, 1970".equals(d.format(EPOCH)), "date FULL -> Thursday, January 1, 1970");

        d = DateFormat.getTimeInstance(DateFormat.SHORT, US); d.setTimeZone(UTC);
        check("12:00\u202FAM".equals(d.format(EPOCH)), "time SHORT -> 12:00 AM");
        d = DateFormat.getTimeInstance(DateFormat.MEDIUM, US); d.setTimeZone(UTC);
        check("12:00:00\u202FAM".equals(d.format(EPOCH)), "time MEDIUM -> 12:00:00 AM");
        d = DateFormat.getTimeInstance(DateFormat.LONG, US); d.setTimeZone(UTC);
        check("12:00:00\u202FAM UTC".equals(d.format(EPOCH)), "time LONG -> 12:00:00 AM UTC");

        d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US); d.setTimeZone(UTC);
        check("1/1/70, 12:00\u202FAM".equals(d.format(EPOCH)), "datetime SHORT/SHORT");
        d = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, US); d.setTimeZone(UTC);
        check("Jan 1, 1970, 12:00:00\u202FAM".equals(d.format(EPOCH)), "datetime MEDIUM/MEDIUM");
    }

    static void testParseLenientAndProperties() throws Exception {
        DateFormat d = DateFormat.getDateInstance(DateFormat.SHORT, US);
        d.setTimeZone(UTC);
        check(d.parse("1/1/70").getTime() == 0L, "parse 1/1/70 -> epoch 0");
        check(d.isLenient(), "default lenient true");
        d.setLenient(false);
        check(!d.isLenient(), "setLenient(false)");
        expectThrows(() -> d.parse("bogus"), ParseException.class, "parse bogus throws ParseException");

        check(UTC.equals(d.getTimeZone()), "getTimeZone returns UTC");
        Calendar cal = new GregorianCalendar(UTC, US);
        d.setCalendar(cal);
        check(UTC.equals(d.getCalendar().getTimeZone()), "setCalendar/getCalendar");
        NumberFormat nf = NumberFormat.getIntegerInstance(US);
        d.setNumberFormat(nf);
        check(d.getNumberFormat() == nf, "setNumberFormat/getNumberFormat");
    }

    static void testEqualsCloneHashCode() {
        DateFormat a = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US);
        a.setTimeZone(UTC);
        DateFormat b = (DateFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals original");
        check(a.hashCode() == b.hashCode(), "clone equal hashCode");
        DateFormat c = DateFormat.getDateInstance(DateFormat.SHORT, US);
        check(!a.equals(c), "date-only != datetime");
        check(!a.equals(new Object()), "!equals foreign object");
    }

    // Minimal concrete subclass to exercise the protected DateFormat() constructor.
    static class MinimalDateFormat extends DateFormat {
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append("d:").append(date.getTime());
        }
        public Date parse(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return new Date(42L);
        }
    }

    static void testBlackBox() {
        MinimalDateFormat mdf = new MinimalDateFormat();
        check(mdf != null, "protected constructor via subclass");
        check("d:0".equals(mdf.format(new Date(0L))), "subclass format");

        ParsePosition pp = new ParsePosition(0);
        Object parsed = mdf.parseObject("anything", pp);
        check(parsed instanceof Date && ((Date) parsed).getTime() == 42L, "subclass parseObject returns Date");
        check(pp.getIndex() == "anything".length(), "subclass parseObject advances position");

        DateFormat real = DateFormat.getDateInstance(DateFormat.SHORT, US);
        real.setTimeZone(UTC);
        ParsePosition pp2 = new ParsePosition(0);
        Object d = real.parseObject("1/1/70", pp2);
        check(d instanceof Date && ((Date) d).getTime() == 0L, "parseObject on real DateFormat");
        check(pp2.getIndex() == "1/1/70".length(), "real parseObject position");

        // ---- boundary/equivalence additions (M2) ----
        DateFormat sh = DateFormat.getDateInstance(DateFormat.SHORT, US);
        sh.setTimeZone(UTC);
        ParsePosition pp3 = new ParsePosition(0);
        Date d3 = sh.parse("1/1/70", pp3);
        check(d3 != null && d3.getTime() == 0L, "parse(String,ParsePosition) result");
        check(pp3.getIndex() == "1/1/70".length(), "parse position full consumption");

        ParsePosition pf = new ParsePosition(0);
        check(sh.parse("junk", pf) == null, "failed parse returns null");
        check(pf.getErrorIndex() == 0, "failed parse errorIndex 0");

        DateFormat t = DateFormat.getTimeInstance(DateFormat.SHORT, US);
        t.setTimeZone(UTC);
        String fs = t.format(EPOCH);
        ParsePosition ptr = new ParsePosition(0);
        check(t.parse(fs, ptr).getTime() == 0L, "time format->parse round-trip");

        check(sh.isLenient(), "default lenient true");
        sh.setLenient(false);
        check(!sh.isLenient(), "setLenient(false)");
        ParsePosition pstrict = new ParsePosition(0);
        check(sh.parse("1/1/70", pstrict).getTime() == 0L, "strict parse valid date works");
        check(sh.getCalendar() != null && UTC.equals(sh.getCalendar().getTimeZone()),
              "getCalendar timezone");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================
    // Fixed probe date: 2024-03-07T14:05:09.123Z

    static Date dfmtProbeDate() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.clear(); c.set(2024, Calendar.MARCH, 7, 14, 5, 9);
        return c.getTime();
    }

    static String dfmt(int dateStyle, int timeStyle, Locale l) {
        DateFormat f = (dateStyle < 0)
                ? DateFormat.getTimeInstance(timeStyle, l)
                : DateFormat.getDateTimeInstance(dateStyle, timeStyle, l);
        if (f instanceof java.text.SimpleDateFormat) ((java.text.SimpleDateFormat) f).setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(dfmtProbeDate());
    }

    static void testStyleConstantsContract() {
        check(DateFormat.FULL == 0, "FULL == 0");
        check(DateFormat.LONG == 1, "LONG == 1");
        check(DateFormat.MEDIUM == 2, "MEDIUM == 2");
        check(DateFormat.SHORT == 3, "SHORT == 3");
        check(DateFormat.DEFAULT == 2, "DEFAULT == MEDIUM");
        check(DateFormat.ERA_FIELD == 0, "ERA_FIELD == 0");
        check(DateFormat.YEAR_FIELD == 1, "YEAR_FIELD == 1");
        check(DateFormat.MINUTE_FIELD == 6, "MINUTE_FIELD == 6");
        check(DateFormat.AM_PM_FIELD == 14, "AM_PM_FIELD == 14");
    }

    static void testDateStyleOutputsContract() {
        check("Thursday, March 7, 2024".equals(withUtc(DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH))),
              "en FULL date includes weekday (identical in JRE and CLDR)");
        check("Mar 7, 2024".equals(withUtc(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH))),
              "en MEDIUM date");
        check("3/7/24".equals(withUtc(DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH))),
              "en SHORT date (identical in JRE and CLDR)");
        check("Mar 7, 2024".equals(withUtc(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH))),
              "en DEFAULT date == MEDIUM");
    }

    static String withUtc(DateFormat f) {
        if (f instanceof java.text.SimpleDateFormat) ((java.text.SimpleDateFormat) f).setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(dfmtProbeDate());
    }

    static void testParseContract() throws java.text.ParseException {
        DateFormat full = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH);
        ((java.text.SimpleDateFormat) full).setTimeZone(TimeZone.getTimeZone("UTC"));
        check(full.parse("Thursday, March 7, 2024").getTime() == 1709769600000L,
              "FULL text -> 2024-03-07T00:00Z");
        DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
        ((java.text.SimpleDateFormat) shortDf).setTimeZone(TimeZone.getTimeZone("UTC"));
        check(shortDf.parse("3/7/24").getTime() == 1709769600000L, "SHORT text -> same instant");
        // text missing the weekday is rejected by the FULL pattern
        expectX(java.text.ParseException.class, () -> full.parse("March 7, 2024"),
                "text without weekday rejected by FULL pattern");
        expectX(java.text.ParseException.class, () -> full.parse("zzz"),
                "garbage -> ParseException");
    }

    static void testWiringContract() throws java.text.ParseException {
        DateFormat f = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
        check(f.getCalendar() != null, "getCalendar non-null");
        Calendar nc = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        f.setCalendar(nc);
        check(f.getCalendar() == nc, "setCalendar/getCalendar same instance");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        check("UTC".equals(f.getTimeZone().getID()), "setTimeZone/getTimeZone");
        check(f.getNumberFormat() != null, "getNumberFormat non-null");
        check(f.isLenient(), "factory instance lenient by default");
        f.setLenient(false);
        check(!f.isLenient(), "setLenient(false)");
        DateFormat strict = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
        ((java.text.SimpleDateFormat) strict).setTimeZone(TimeZone.getTimeZone("UTC"));
        strict.setLenient(false);
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(strict.parse("2/30/24", pp) == null && pp.getErrorIndex() == 7 && pp.getIndex() == 0,
              "strict SHORT parse of 2/30/24: null, errorIndex 7");
        check(DateFormat.getInstance() instanceof java.text.SimpleDateFormat, "getInstance() -> SimpleDateFormat");
        check(DateFormat.getDateInstance().clone().equals(DateFormat.getDateInstance()),
              "clone equals same-style factory instance");
    }

    static void testNullAndIllegalContract() {
        expectX(NullPointerException.class, () -> DateFormat.getDateInstance(DateFormat.FULL, null),
                "getDateInstance(style, null) -> NPE");
        expectX(IllegalArgumentException.class, () -> DateFormat.getDateInstance(99, Locale.ENGLISH),
                "style 99 -> IAE");
        check(DateFormat.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
        check(java.util.Arrays.asList(DateFormat.getAvailableLocales()).contains(Locale.ENGLISH),
              "available contains ENGLISH");
    }

    static void testVersionLockedStyleOutputsContract() {
        // JDK-27 CLDR data: NNBSP before AM/PM, localized zone names, comma before time
        check("2:05:09\u202FPM Coordinated Universal Time"
              .equals(withUtc(DateFormat.getTimeInstance(DateFormat.FULL, Locale.ENGLISH))),
              "en FULL time (CLDR)");
        check("2:05:09\u202FPM".equals(withUtc(DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.ENGLISH))),
              "en MEDIUM time (CLDR)");
        check("2:05\u202FPM".equals(withUtc(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ENGLISH))),
              "en SHORT time (CLDR)");
        check("March 7, 2024, 2:05:09\u202FPM"
              .equals(dfmt(DateFormat.LONG, DateFormat.MEDIUM, Locale.ENGLISH)),
              "en LONG date + MEDIUM time (CLDR, comma + NNBSP)");
        check("3/7/24, 2:05\u202FPM".equals(dfmt(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH)),
              "en SHORT+SHORT (CLDR)");
        check("14:05:09 Koordinierte Weltzeit"
              .equals(withUtc(DateFormat.getTimeInstance(DateFormat.FULL, Locale.GERMAN))),
              "de FULL time has localized zone (CLDR)");
        check("14:05:09 temps universel coordonn\u00E9"
              .equals(withUtc(DateFormat.getTimeInstance(DateFormat.FULL, Locale.FRENCH))),
              "fr FULL time has localized zone (CLDR)");
        check("MMM d, y".equals(((java.text.SimpleDateFormat)
              DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH)).toPattern()),
              "en default toPattern uses y (CLDR)");
        check("07/03/2024".equals(withUtc(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRENCH))),
              "fr SHORT date is 4-digit year (CLDR)");
        check("dd.MM.y".equals(((java.text.SimpleDateFormat)
              DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.GERMAN)).toPattern()),
              "de default toPattern (CLDR)");
    }
    public static void main(String[] args) throws Exception {
        testStyleConstantsContract();
        testDateStyleOutputsContract();
        testParseContract();
        testWiringContract();
        testNullAndIllegalContract();

        check("2024\u5E743\u67087\u65E5\u6728\u66DC\u65E5"
              .equals(withUtc(DateFormat.getDateInstance(DateFormat.FULL, Locale.JAPANESE))),
              "ja FULL date appends weekday (CLDR)");
        testVersionLockedStyleOutputsContract();
        testWhiteBox();
        testFactoryFormatStrings();
        testParseLenientAndProperties();
        testEqualsCloneHashCode();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
