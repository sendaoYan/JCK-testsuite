/*
 * @test
 * @summary java.text.SimpleDateFormat conformance: white-box structure + black-box pattern/format/parse behavior.
 * @run main SimpleDateFormatTest
 */

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

    static void testBlackBox() {
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

        // ---- boundary/equivalence additions (M2) ----
        GregorianCalendar cal = new GregorianCalendar(UTC, US);
        cal.clear();
        cal.set(2000, Calendar.FEBRUARY, 29, 0, 0, 0);
        Date leap = cal.getTime();
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd", US);
        ymd.setTimeZone(UTC);
        check("2000-02-29".equals(ymd.format(leap)), "leap day format");
        ParsePosition pLeap = new ParsePosition(0);
        check(ymd.parse("2000-02-29", pLeap).getTime() == leap.getTime(), "leap day parse round-trip");

        cal.clear();
        cal.set(2001, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endOfYear = cal.getTime();
        SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", US);
        ts.setTimeZone(UTC);
        check("2001-12-31 23:59:59".equals(ts.format(endOfYear)), "year-end format");

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
    // Fixed probe date: 2024-03-07T14:05:09.123Z (Thursday, week 10, day-of-year 67)

    static Date probeDate() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.clear(); c.set(2024, Calendar.MARCH, 7, 14, 5, 9); c.set(Calendar.MILLISECOND, 123);
        return c.getTime();
    }

    static String fmt(String pattern, Locale l, TimeZone tz) {
        SimpleDateFormat f = new SimpleDateFormat(pattern, l);
        f.setTimeZone(tz);
        return f.format(probeDate());
    }

    static void testPatternLetterMatrixContract() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        String[][] tbl = {
            {"yyyy-MM-dd HH:mm:ss.SSS", "2024-03-07 14:05:09.123"},
            {"yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "2024-03-07T14:05:09.123Z"},
            {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "2024-03-07T14:05:09.123Z"},
            {"EEE", "Thu"}, {"EEEE", "Thursday"}, {"MMM", "Mar"}, {"MMMM", "March"},
            {"a", "PM"}, {"Z", "+0000"}, {"ZZZZZ", "+0000"}, {"z", "UTC"}, {"zzz", "UTC"},
            {"zzzz", "Coordinated Universal Time"}, {"G", "AD"}, {"GGGG", "AD"},
            {"w", "10"}, {"W", "2"}, {"D", "67"}, {"F", "1"}, {"E", "Thu"}, {"u", "4"},
            {"dd/MM/yy", "07/03/24"}, {"yy", "24"}, {"y", "2024"}, {"yyyyy", "02024"},
            {"hh:mm a", "02:05 PM"}, {"KK:mm a", "02:05 PM"}, {"kk", "14"}, {"H", "14"},
            {"hh", "02"}, {"KK", "02"}, {"mm", "05"}, {"ss", "09"}, {"SSS", "123"},
            {"MMMyyyy", "Mar2024"}, {"D yyyy", "67 2024"}, {"ww yyyy", "10 2024"},
            {"W MM/yyyy", "2 03/2024"}, {"F EEE MM/yyyy", "1 Thu 03/2024"}, {"G yyyy", "AD 2024"},
            {"dd-MM-yyyy GGGG", "07-03-2024 AD"},
            {"yyyy MM dd HH:mm:ss z", "2024 03 07 14:05:09 UTC"},
            {"yyyy.MM.dd G 'at' HH:mm:ss z", "2024.03.07 AD at 14:05:09 UTC"},
            {"EEE, MMM d, ''yy", "Thu, Mar 7, '24"},
            {"h:mm a", "2:05 PM"},
        };
        for (String[] row : tbl)
            check(row[1].equals(fmt(row[0], Locale.ENGLISH, utc)),
                  "<" + row[0] + "> -> " + row[1] + " got " + fmt(row[0], Locale.ENGLISH, utc));
        // unknown pattern letter -> IllegalArgumentException at construction
        expectX(IllegalArgumentException.class, () -> new SimpleDateFormat("n?", Locale.ENGLISH),
                "unknown pattern letter -> IAE");
        expectX(IllegalArgumentException.class, () -> new SimpleDateFormat("q", Locale.ENGLISH),
                "letter q -> IAE");
    }

    static void testZoneDisplayNameContract() {
        TimeZone sh = TimeZone.getTimeZone("Asia/Shanghai");
        check("CST".equals(fmt("z", Locale.ENGLISH, sh)), "Asia/Shanghai short name (EN)");
        check("China Standard Time".equals(fmt("zzzz", Locale.ENGLISH, sh)), "Asia/Shanghai long name (EN)");
        check("+0800".equals(fmt("Z", Locale.ENGLISH, sh)), "RFC822 offset");
        check("+08:00".equals(fmt("XXX", Locale.ENGLISH, sh)), "ISO offset with colon");
        check("\u4E2D\u56FD\u6807\u51C6\u65F6\u95F4".equals(fmt("zzzz", Locale.SIMPLIFIED_CHINESE, sh)),
              "Asia/Shanghai long name (zh-CN)");
        check("CST".equals(fmt("zzz", Locale.SIMPLIFIED_CHINESE, sh)),
              "Asia/Shanghai short name falls back to ID abbreviation (zh-CN)");
    }

    static void testLenientSemanticsContract() throws java.text.ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(f.isLenient(), "lenient by default");
        // lenient rolls 2024-02-30 into March 1
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.setTime(f.parse("2024-02-30"));
        check(c.get(Calendar.MONTH) == Calendar.MARCH && c.get(Calendar.DAY_OF_MONTH) == 1,
              "lenient parse of Feb 30 -> Mar 1");
        // strict mode rejects with exact error offsets
        f.setLenient(false);
        check(!f.isLenient(), "setLenient(false)");
        try {
            f.parse("2024-02-30");
            throw new AssertionError("strict 2024-02-30 should throw");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 10, "strict Feb-30 errorOffset=10");
        }
        try {
            f.parse("2024-13-01");
            throw new AssertionError("strict month 13 should throw");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 10, "strict month-13 errorOffset=10");
        }
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(f.parse("2024-02-30", pp) == null && pp.getIndex() == 0 && pp.getErrorIndex() == 10,
              "strict ParsePosition variant: null result, index kept, errorIndex 10");
        SimpleDateFormat h = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        h.setTimeZone(TimeZone.getTimeZone("UTC"));
        h.setLenient(false);
        java.text.ParsePosition ph = new java.text.ParsePosition(0);
        check(h.parse("2024-03-07 25:00:00", ph) == null && ph.getIndex() == 0 && ph.getErrorIndex() == 19,
              "hour 25 strict: errorIndex at seconds field");
        h.setLenient(true);
        java.text.ParsePosition ph2 = new java.text.ParsePosition(0);
        check(h.parse("2024-03-07 25:00:00", ph2) != null && ph2.getIndex() == 19 && ph2.getErrorIndex() == -1,
              "hour 25 lenient rolls to next day, full index consumed");
        // leap second rolls over in lenient mode
        SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        s.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(s.parse("09:59:60").getTime() == 10L * 3600 * 1000, "leap second 09:59:60 -> 10:00:00 UTC");
        s.setLenient(false);
        java.text.ParsePosition pl = new java.text.ParsePosition(0);
        check(s.parse("09:59:60", pl) == null && pl.getErrorIndex() == 8, "leap second strict errorIndex=8");
    }

    static void testTwoDigitYearWindowContract() throws java.text.ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yy", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.setTime(f.parse("24"));
        check(c.get(Calendar.YEAR) == 2024, "yy=24 -> 2024 (within default window)");
        c.setTime(f.parse("80"));
        check(c.get(Calendar.YEAR) == 1980, "yy=80 -> 1980 (before window start)");
        c.setTime(f.parse("39"));
        check(c.get(Calendar.YEAR) == 2039, "yy=39 -> 2039 (after window end)");
        SimpleDateFormat g = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);
        g.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat anchor = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        anchor.setTimeZone(TimeZone.getTimeZone("UTC"));
        g.set2DigitYearStart(anchor.parse("2100-01-01"));
        c.setTime(g.parse("03/07/24"));
        check(c.get(Calendar.YEAR) == 2124, "2DigitYearStart(2100): 24 -> 2124");
        check(g.get2DigitYearStart().getTime() == anchor.parse("2100-01-01").getTime(),
              "get2DigitYearStart round-trips");
        c.setTime(g.parse("03/07/99"));
        check(c.get(Calendar.YEAR) == 2199, "2DigitYearStart(2100): 99 -> 2199 (window [2100,2200))");
        c.setTime(g.parse("03/07/00"));
        check(c.get(Calendar.YEAR) == 2100, "2DigitYearStart(2100): 00 -> 2100 window start");
    }

    static void testParsePositionMatrixContract() throws java.text.ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        // trailing text: parse stops at 10, no exception
        java.text.ParsePosition t = new java.text.ParsePosition(0);
        check(f.parse("2024-03-07rest", t) != null && t.getIndex() == 10,
              "trailing garbage: index stops at end of pattern");
        // parse can start mid-string
        java.text.ParsePosition off = new java.text.ParsePosition(5);
        check(f.parse("xx 2024-03-07 yy", off) != null && off.getIndex() == 13,
              "parse from explicit offset");
        // unparseable month records errorIndex and returns null
        java.text.ParsePosition bad = new java.text.ParsePosition(0);
        check(f.parse("2024-XX-07", bad) == null && bad.getIndex() == 0 && bad.getErrorIndex() == 5,
              "bad month: null, errorIndex=5");
        // day names and month names round-trip
        SimpleDateFormat n = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);
        n.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.setTime(n.parse("Thu Mar 07 2024"));
        check(c.get(Calendar.YEAR) == 2024 && c.get(Calendar.MONTH) == Calendar.MARCH
              && c.get(Calendar.DAY_OF_MONTH) == 7, "name-based parse");
        // adjacent numeric fields parse
        SimpleDateFormat adj = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        adj.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(adj.parse("20240307") != null, "adjacent digit groups parse");
        // ISO offset parsing shifts millis: 2024-03-07T00:00+08:00 == 2024-03-06T16:00Z
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-ddXXX", Locale.ENGLISH);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(iso.parse("2024-03-07+08:00").getTime() == 1709769600000L - 8L * 3600 * 1000,
              "XXX offset parse adjusts instant");
    }

    static void testEqualsCloneHashCodeContract() {
        SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat b = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        a.setTimeZone(TimeZone.getTimeZone("UTC"));
        b.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equal pattern+locale+tz -> equals/hashCode");
        b.setLenient(false);
        check(!a.equals(b), "lenient participates in equals");
        SimpleDateFormat cl = (SimpleDateFormat) a.clone();
        check(cl.equals(a), "clone equals");
        SimpleDateFormat otherLocale = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);
        otherLocale.setTimeZone(TimeZone.getTimeZone("UTC"));
        check(!a.equals(otherLocale), "locale participates in equals");
        cl.applyPattern("dd/MM/yyyy");
        check(!cl.equals(a), "pattern participates in equals");
    }

    static void testSymbolsWiringContract() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        check(f.getDateFormatSymbols() != null, "getDateFormatSymbols non-null");
        check(f.getDateFormatSymbols() != f.getDateFormatSymbols(),
              "getDateFormatSymbols returns a copy each call");
        java.text.DateFormatSymbols mut = (java.text.DateFormatSymbols) f.getDateFormatSymbols().clone();
        mut.setAmPmStrings(new String[]{"ante", "post"});
        f.setDateFormatSymbols(mut);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        check("post".equals(new SimpleDateFormat("a", mut) {{ setTimeZone(TimeZone.getTimeZone("UTC")); }}
              .format(probeDate())), "setDateFormatSymbols affects am/pm output");
        check(f.getNumberFormat() != null, "getNumberFormat non-null");
        f.setNumberFormat(java.text.NumberFormat.getIntegerInstance(Locale.GERMANY));
        check("2024-03-07".equals(f.format(probeDate())), "setNumberFormat accepted, format still works");
    }

    static void testNullAndBogusContract() {
        expectX(NullPointerException.class, () -> new SimpleDateFormat((String) null),
                "ctor(null pattern) -> NPE");
        expectX(NullPointerException.class, () -> new SimpleDateFormat("yyyy", (Locale) null),
                "ctor(null locale) -> NPE");
        expectX(NullPointerException.class,
                () -> new SimpleDateFormat("yyyy", (java.text.DateFormatSymbols) null),
                "ctor(null symbols) -> NPE");
        SimpleDateFormat f = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        expectX(NullPointerException.class, () -> f.applyPattern(null), "applyPattern(null) -> NPE");
        check(new SimpleDateFormat("yyyy", new Locale("x", "Y")).format(probeDate()) != null,
              "unknown locale falls back gracefully");
    }

    static void testFieldRunStructureContract() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        AttributedCharacterIterator it = f.formatToCharacterIterator(probeDate());
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("2024-03-07 14:05".equals(sb.toString()), "formatToCharacterIterator text");
        it.first();
        check(it.getRunStart() == 0 && it.getRunLimit() == 4
              && it.getAttributes().containsKey(java.text.DateFormat.Field.YEAR), "year run [0,4)");
        it.setIndex(5);
        check(it.getRunStart() == 5 && it.getRunLimit() == 7
              && it.getAttributes().containsKey(java.text.DateFormat.Field.MONTH), "month run [5,7)");
        it.setIndex(8);
        check(it.getRunStart() == 8 && it.getRunLimit() == 10
              && it.getAttributes().containsKey(java.text.DateFormat.Field.DAY_OF_MONTH), "day run [8,10)");
        it.setIndex(11);
        check(it.getRunStart() == 11 && it.getRunLimit() == 13
              && it.getAttributes().containsKey(java.text.DateFormat.Field.HOUR_OF_DAY0), "hour run [11,13)");
        it.setIndex(14);
        check(it.getRunStart() == 14 && it.getRunLimit() == 16
              && it.getAttributes().containsKey(java.text.DateFormat.Field.MINUTE), "minute run [14,16)");
        java.text.FieldPosition fy = new java.text.FieldPosition(java.text.DateFormat.Field.YEAR);
        f.format(probeDate(), new StringBuffer(), fy);
        check(fy.getBeginIndex() == 0 && fy.getEndIndex() == 4, "YEAR FieldPosition [0,4)");
        java.text.FieldPosition fm = new java.text.FieldPosition(java.text.DateFormat.Field.MONTH);
        f.format(probeDate(), new StringBuffer(), fm);
        check(fm.getBeginIndex() == 5 && fm.getEndIndex() == 7, "MONTH FieldPosition [5,7)");
    }

    static void testVersionLockedLocaleDataContract() {
        // JDK-27 CLDR locale data
        check("M/d/yy, h:mm\u202Fa".equals(new SimpleDateFormat().toPattern()),
              "default pattern has comma + NNBSP (CLDR)");
        check("Donnerstag M\u00E4rz PM n. Chr. UTC"
              .equals(fmt("EEEE MMMM a G z", Locale.GERMAN, TimeZone.getTimeZone("UTC"))),
              "de names (CLDR)");
        check("jeudi mars PM ap. J.-C. UTC"
              .equals(fmt("EEEE MMMM a G z", Locale.FRENCH, TimeZone.getTimeZone("UTC"))),
              "fr names (CLDR)");
        check("\u6728\u66DC\u65E5 3\u6708 \u5348\u5F8C \u897F\u66A6 UTC"
              .equals(fmt("EEEE MMMM a G z", Locale.JAPANESE, TimeZone.getTimeZone("UTC"))),
              "ja names (CLDR)");
        check("gioved\u00EC marzo PM d.C. UTC"
              .equals(fmt("EEEE MMMM a G z", Locale.ITALIAN, TimeZone.getTimeZone("UTC"))),
              "it era abbreviation differs from JRE (CLDR)");
        // CLDR localized pattern characters equal the standard ones for de/ar-EG
        SimpleDateFormat de = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
        check("yyyy-MM-dd".equals(de.toLocalizedPattern()), "de localized pattern chars (CLDR)");
        SimpleDateFormat ar = new SimpleDateFormat("yyyy-MM-dd", new Locale("ar", "EG"));
        check("yyyy-MM-dd".equals(ar.toLocalizedPattern()), "ar-EG localized pattern chars (CLDR)");
        SimpleDateFormat ap = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
        ap.setTimeZone(TimeZone.getTimeZone("UTC"));
        ap.applyLocalizedPattern("dd.MM.yyyy");
        check("dd.MM.yyyy".equals(ap.toPattern()) && "07.03.2024".equals(ap.format(probeDate())),
              "applyLocalizedPattern with standard chars (CLDR)");
        expectX(IllegalArgumentException.class,
                () -> new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).applyLocalizedPattern("tt.MM.uuuu"),
                "legacy JRE chars rejected in de localized pattern (CLDR)");
    }
    public static void main(String[] args) throws Exception {
        testPatternLetterMatrixContract();
        testZoneDisplayNameContract();
        testLenientSemanticsContract();
        testTwoDigitYearWindowContract();
        testParsePositionMatrixContract();
        testEqualsCloneHashCodeContract();
        testSymbolsWiringContract();
        testNullAndBogusContract();
        testFieldRunStructureContract();
        testVersionLockedLocaleDataContract();
        testWhiteBox();
        testPatternLetters();
        testRoundTrip();
        testTwoDigitYear();
        testPatternMutation();
        testCharacterIteratorAndSymbols();
        testEqualsCloneHashCode();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
