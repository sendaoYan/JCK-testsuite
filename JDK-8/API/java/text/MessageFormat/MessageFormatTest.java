/*
 * @test
 * @summary java.text.MessageFormat API conformance: full contract coverage.
 *          basic substitution + static format, number/choice subformats,
 *          setFormatByArgumentIndex/setFormatsByArgumentIndex with fixed SimpleDateFormat
 *          (UTC), parse (String/Number types), applyPattern/toPattern/setLocale/getLocale,
 *          formatToCharacterIterator, equals/hashCode/clone, invalid patterns ->
 *          IllegalArgumentException, missing args, boundary numeric formats, empty parse,
 *          setFormat/setFormats/parseObject, and serialization round-trip.
 * @run main MessageFormatTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessageFormatTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.MessageFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check("java.text.Format".equals(c.getSuperclass().getName()), "extends Format");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Locale.class) != null, "ctor(String,Locale)");
        check(c.getMethod("setFormatsByArgumentIndex", Format[].class) != null,
              "setFormatsByArgumentIndex(Format[])");
        check(c.getMethod("format", Object[].class, StringBuffer.class, FieldPosition.class) != null,
              "format(Object[],...)");
        check(c.getMethod("format", String.class, Object[].class) != null,
              "static format(String,Object...)");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBasicSubstitution() {
        MessageFormat mf = new MessageFormat("Hello {0}, you have {1} messages.", US);
        check("Hello World, you have 3 messages.".equals(mf.format(new Object[]{"World", 3})),
              "basic substitution");
        check("x=1, y=2".equals(MessageFormat.format("x={0}, y={1}", 1, 2)), "static format");

        StringBuffer sb = new StringBuffer();
        sb = new MessageFormat("a{0}b", US).format(new Object[]{"X"}, sb, new FieldPosition(0));
        check("aXb".equals(sb.toString()), "format(Object[],StringBuffer,FieldPosition)");
    }

    static void testConstructorsAndExceptions() {
        expectThrows(IllegalArgumentException.class, () -> new MessageFormat("{"),
                     "unmatched open brace");
        expectThrows(IllegalArgumentException.class, () -> new MessageFormat("{0"),
                     "unclosed placeholder");
        expectThrows(NullPointerException.class, () -> new MessageFormat(null),
                     "ctor(null) -> NPE");
        expectThrows(NullPointerException.class, () -> new MessageFormat(null, US),
                     "ctor(null, Locale) -> NPE");
    }

    static void testSubformats() {
        check("1234.57".equals(new MessageFormat("{0,number,#.##}", US).format(new Object[]{1234.567})),
              "number subformat");

        MessageFormat choice = new MessageFormat("{0,choice,0#zero|1#one|2#many}", US);
        check("zero".equals(choice.format(new Object[]{0})), "choice 0 -> zero");
        check("one".equals(choice.format(new Object[]{1})), "choice 1 -> one");
        check("many".equals(choice.format(new Object[]{2})), "choice 2 -> many");
        check("many".equals(choice.format(new Object[]{5})), "choice 5 -> many");
    }

    static void testExplicitFormats() {
        MessageFormat mf = new MessageFormat("{0} at {1}", US);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", US);
        sdf.setTimeZone(UTC);
        mf.setFormatByArgumentIndex(1, sdf);
        check("X at 1970-01-01 00:00:00".equals(mf.format(new Object[]{"X", EPOCH})),
              "setFormatByArgumentIndex fixed date");

        MessageFormat multi = new MessageFormat("{0} {1} {2}", US);
        NumberFormat nf = NumberFormat.getIntegerInstance(US);
        ChoiceFormat cf = new ChoiceFormat("0#no|1#yes");
        SimpleDateFormat year = new SimpleDateFormat("yyyy", US);
        year.setTimeZone(UTC);
        multi.setFormatsByArgumentIndex(new Format[]{nf, cf, year});
        check("42 yes 1970".equals(multi.format(new Object[]{42, 1, EPOCH})),
              "setFormatsByArgumentIndex array");
        Format[] fmts = multi.getFormatsByArgumentIndex();
        check(fmts != null && fmts.length == 3, "getFormatsByArgumentIndex length 3");
    }

    static void testParse() throws Exception {
        Object[] parts = new MessageFormat("x={0}, y={1}", US).parse("x=1, y=2");
        check(parts.length == 2, "parse length 2");
        check("1".equals(parts[0]) && "2".equals(parts[1]), "parse literal -> String values");

        Object[] num = new MessageFormat("{0,number} and {1,number}", US).parse("42 and 3.5");
        check(num.length == 2, "number parse length");
        check(num[0].getClass() == Long.class && num[1].getClass() == Double.class, "number parse types");
        check(((Number) num[0]).longValue() == 42L && ((Number) num[1]).doubleValue() == 3.5,
              "number parse values");

        Object[] emptyParsed = new MessageFormat("{0}", US).parse("", new ParsePosition(0));
        check(emptyParsed.length == 1 && "".equals(emptyParsed[0]), "parse empty yields one empty element");
    }

    static void testPatternAndLocale() {
        MessageFormat mf = new MessageFormat("{0}", US);
        check("{0}".equals(mf.toPattern()), "toPattern");
        check(US.equals(mf.getLocale()), "getLocale US");
        mf.setLocale(Locale.FRANCE);
        check(Locale.FRANCE.equals(mf.getLocale()), "setLocale/getLocale");
        mf.setLocale(US);
        mf.applyPattern("A {0} B {1}");
        check("A {0} B {1}".equals(mf.toPattern()), "applyPattern");
        check("A x B y".equals(mf.format(new Object[]{"x", "y"})), "applied pattern format");
    }

    static void testIteratorEqualsClone() {
        MessageFormat mf = new MessageFormat("Hi {0}", US);
        AttributedCharacterIterator it = mf.formatToCharacterIterator(new Object[]{"X"});
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("Hi X".equals(sb.toString()), "formatToCharacterIterator");

        MessageFormat a = new MessageFormat("{0}-{1}", US);
        MessageFormat b = (MessageFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.equals(new MessageFormat("{0}-{1}", US)), "same pattern equal");
        check(!a.equals(new MessageFormat("{0}", US)), "different pattern not equal");
    }

    static void testBoundaryValues() {
        check("".equals(new MessageFormat("").format(new Object[]{})), "empty pattern formats empty");
        check("a {1}".equals(MessageFormat.format("{0} {1}", "a")), "missing arg leaves placeholder");
        check("0".equals(MessageFormat.format("{0,number,0}", 0)), "format 0");
        check("2147483647".equals(MessageFormat.format("{0,number,0}", Integer.MAX_VALUE)),
              "format Integer.MAX_VALUE");
        check("-2147483648".equals(MessageFormat.format("{0,number,0}", Integer.MIN_VALUE)),
              "format Integer.MIN_VALUE");
        check("9223372036854775807".equals(MessageFormat.format("{0,number,0}", Long.MAX_VALUE)),
              "format Long.MAX_VALUE");
    }

    static void testSetFormatAndParseObject() {
        MessageFormat mf = new MessageFormat("{0} {1} {2}", US);
        NumberFormat nf = NumberFormat.getIntegerInstance(US);
        mf.setFormat(1, nf);
        Format[] g = mf.getFormats();
        check(g != null && g.length == 3, "getFormats length 3 after setFormat");
        check(g[1] == nf, "setFormat(1, NumberFormat) applied at index 1");

        mf.setFormats(new Format[]{nf, nf, nf});
        Format[] all = mf.getFormats();
        check(all != null && all.length == 3, "setFormats/getFormats length 3");
        check(all[0] == nf && all[1] == nf && all[2] == nf, "setFormats applied to every element");
        check("7 8 9".equals(mf.format(new Object[]{7, 8, 9})), "setFormats formats integers");

        ParsePosition pp = new ParsePosition(0);
        Object r = mf.parseObject("11 22 33", pp);
        check(r instanceof Object[], "parseObject returns Object[]");
        Object[] parts = (Object[]) r;
        check(parts.length == 3, "parseObject length 3");
        check(((Number) parts[0]).longValue() == 11L && ((Number) parts[2]).longValue() == 33L,
              "parseObject number values");
        check(pp.getIndex() == "11 22 33".length(), "parseObject advances index");

        MessageFormat plain = new MessageFormat("{0} {1} {2}", US);
        ParsePosition pp2 = new ParsePosition(0);
        Object[] strParts = (Object[]) plain.parseObject("a b c", pp2);
        check(strParts.length == 3, "plain parseObject length 3");
        check("a".equals(strParts[0]) && "c".equals(strParts[2]), "plain parseObject String values");
        check(pp2.getIndex() == "a b c".length(), "plain parseObject advances index");
    }

    static void testSerialization() throws Exception {
        MessageFormat mf = new MessageFormat("{0}-{1}", US);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(mf);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MessageFormat r = (MessageFormat) ois.readObject();
        check("{0}-{1}".equals(r.toPattern()), "round-trip preserves pattern");
        check("a-b".equals(r.format(new Object[]{"a", "b"})), "round-trip still formats");
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
    // Fixed probe date: 2024-03-07T14:05:09Z

    static java.util.Date mfProbeDate() {
        java.util.Calendar c = java.util.Calendar.getInstance(
                java.util.TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.clear(); c.set(2024, java.util.Calendar.MARCH, 7, 14, 5, 9);
        return c.getTime();
    }

    static void testTypedSubformatsContract() {
        check("3.14".equals(new MessageFormat("{0,number,#.##}", Locale.ENGLISH)
              .format(new Object[]{3.14159})), "number #.##");
        check("3.14".equals(new MessageFormat("{0,number,0.00}", Locale.US)
              .format(new Object[]{3.14159})), "number 0.00");
        check("4".equals(new MessageFormat("{0,number,integer}", Locale.US)
              .format(new Object[]{3.7})), "number integer rounds HALF_EVEN");
        check("$3.70".equals(new MessageFormat("{0,number,currency}", Locale.US)
              .format(new Object[]{3.7})), "number currency");
        check("37%".equals(new MessageFormat("{0,number,percent}", Locale.US)
              .format(new Object[]{0.37})), "number percent");
        // time subformats default to the JVM default zone: pin it explicitly via getFormats
        MessageFormat t = new MessageFormat("{0,time,HH:mm:ss}", Locale.ENGLISH);
        ((java.text.DateFormat) t.getFormats()[0]).setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("14:05:09".equals(t.format(new Object[]{mfProbeDate()})), "time subformat honors tz");
        MessageFormat d = new MessageFormat("{0,date,yyyy-MM-dd}", Locale.ENGLISH);
        ((java.text.DateFormat) d.getFormats()[0]).setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("2024-03-07".equals(d.format(new Object[]{mfProbeDate()})), "date subformat honors tz");
        MessageFormat c = new MessageFormat("{0,choice,0#none|1#one|1<many}", Locale.ENGLISH);
        check("none".equals(c.format(new Object[]{0})), "choice 0");
        check("one".equals(c.format(new Object[]{1})), "choice 1");
        check("many".equals(c.format(new Object[]{5})), "choice 5");
    }

    static void testQuotingRulesContract() {
        check("it's x".equals(new MessageFormat("it''s {0}", Locale.ENGLISH).format(new Object[]{"x"})),
              "'' produces one quote");
        check("{0}".equals(new MessageFormat("'{0}'", Locale.ENGLISH).format(new Object[]{"x"})),
              "quoted placeholder is literal, argument swallowed");
        check("{x}".equals(new MessageFormat("'{'{0}'}'", Locale.ENGLISH).format(new Object[]{"x"})),
              "'{' and '}' escape braces");
        check("'v'".equals(new MessageFormat("''{0}''", Locale.ENGLISH).format(new Object[]{"v"})),
              "quoted quotes around placeholder");
        check("{'}".equals(new MessageFormat("'{''}'", Locale.ENGLISH).format(new Object[]{})),
              "brace, quote, brace literals");
        check("a quote x".equals(new MessageFormat("a 'quote' {0}", Locale.ENGLISH)
              .format(new Object[]{"x"})), "quoted plain text");
    }

    static void testArgumentIndexContract() {
        check("x".equals(new MessageFormat("{0}", Locale.ENGLISH).format(new Object[]{"x"})), "{0}");
        check("y".equals(new MessageFormat("{1}", Locale.ENGLISH).format(new Object[]{"x", "y", "z"})),
              "{1} picks second");
        check("x x".equals(new MessageFormat("{0} {0}", Locale.ENGLISH).format(new Object[]{"x"})),
              "repeated argument");
        check("xyz".equals(new MessageFormat("{0}{1}{2}", Locale.ENGLISH)
              .format(new Object[]{"x", "y", "z"})), "concatenation");
        // missing arguments: out-of-range placeholders stay literal
        check("{5}".equals(new MessageFormat("{5}", Locale.ENGLISH).format(new Object[]{"a"})),
              "out-of-range index stays literal (no throw)");
        check("only{1}{2}".equals(new MessageFormat("{0}{1}{2}", Locale.ENGLISH)
              .format(new Object[]{"only"})), "trailing missing arguments stay literal");
        // null argument formats as "null"
        check("x null".equals(new MessageFormat("{0} {1}", Locale.ENGLISH)
              .format(new Object[]{"x", null})), "null argument prints null");
    }

    static void testIllegalPatternsContract() {
        expectX(IllegalArgumentException.class, () -> new MessageFormat("{0,unknown}"),
                "unknown format type at construction -> IAE");
        try {
            new MessageFormat("{0,unknown}");
            throw new AssertionError("unreachable");
        } catch (IllegalArgumentException iae) {
            check("unknown format type: unknown".equals(iae.getMessage()),
                  "unknown format type message exact");
        }
        expectX(IllegalArgumentException.class, () -> new MessageFormat("{0"),
                "unmatched brace -> IAE");
        try {
            new MessageFormat("{0");
            throw new AssertionError("unreachable");
        } catch (IllegalArgumentException iae) {
            check("Unmatched braces in the pattern.".equals(iae.getMessage()),
                  "unmatched brace message exact");
        }
        // unmatched close brace is literal
        check("a}".equals(new MessageFormat("{0}}", Locale.ENGLISH).format(new Object[]{"a"})),
              "stray close brace is literal");
    }

    static void testParseContract() throws java.text.ParseException {
        MessageFormat m = new MessageFormat("{0} and {1}", Locale.ENGLISH);
        Object[] p = m.parse("x and y");
        check(p.length == 2 && "x".equals(p[0]) && "y".equals(p[1]), "two-argument parse");
        MessageFormat n = new MessageFormat("{0,number,#.##}", Locale.US);
        Object[] pn = n.parse("3.14");
        check(pn[0] instanceof Double && 3.14 == (Double) pn[0], "number parse -> Double");
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(m.parse("nomatch", pp) == null && pp.getIndex() == 0 && pp.getErrorIndex() == 0,
              "failed parse: null result, index 0, errorIndex 0");
        expectX(java.text.ParseException.class, () -> m.parse("nomatch"),
                "parse(String) throws on total mismatch");
    }

    static void testLocaleSwitchingContract() {
        MessageFormat m = new MessageFormat("{0,number,#.##}", Locale.US);
        check("3.14".equals(m.format(new Object[]{3.14159})), "US decimal point");
        m.setLocale(Locale.GERMANY);
        check(Locale.GERMANY.equals(m.getLocale()), "getLocale after setLocale");
        // locale change only takes effect after applyPattern
        check("3.14".equals(m.format(new Object[]{3.14159})),
              "formats keep old locale until applyPattern");
        m.applyPattern("{0,number,#.##}");
        check("3,14".equals(m.format(new Object[]{3.14159})),
              "applyPattern rebinds subformat to new locale");
    }

    static void testFormatsAccessContract() {
        MessageFormat m = new MessageFormat("{0} {1,number,0.0} {2,date,short}", Locale.US);
        Format[] byArg = m.getFormatsByArgumentIndex();
        check(byArg.length == 3 && byArg[0] == null, "argument 0 has no format");
        check(byArg[1] instanceof java.text.DecimalFormat, "argument 1 is DecimalFormat");
        check(byArg[2] instanceof java.text.DateFormat, "argument 2 is DateFormat");
        Format[] all = m.getFormats();
        check(all.length == 3, "getFormats length");
        MessageFormat s = new MessageFormat("{0,number}", Locale.US);
        s.setFormat(0, new java.text.DecimalFormat("0.00"));
        check("3.14".equals(s.format(new Object[]{3.14159})), "setFormat replaces subformat");
        // clone independence
        MessageFormat orig = new MessageFormat("{0,number,#.##}", Locale.US);
        MessageFormat cl = (MessageFormat) orig.clone();
        check(cl.equals(orig), "clone equals");
        cl.setFormat(0, new java.text.DecimalFormat("0"));
        check(!cl.format(new Object[]{3.7}).equals(orig.format(new Object[]{3.7})),
              "clone format mutation does not leak");
        check(new MessageFormat("{0}", Locale.US).equals(new MessageFormat("{0}", Locale.US)),
              "equal patterns equal");
        check(!new MessageFormat("{0}", Locale.US).equals(new MessageFormat("{1}", Locale.US)),
              "different patterns not equal");
    }

    static void testStaticAndObjectFormatContract() {
        check("a-b".equals(MessageFormat.format("{0}-{1}", "a", "b")), "static format varargs");
        MessageFormat m = new MessageFormat("{0}-{1}", Locale.ENGLISH);
        check("x-y".equals(m.format((Object) new Object[]{"x", "y"})),
              "Format.format(Object) single-argument convenience");
    }

    static void testFieldConstantsContract() {
        check(MessageFormat.Field.ARGUMENT != null, "Field.ARGUMENT present");
        check("java.text.MessageFormat$Field(message argument field)".equals(MessageFormat.Field.ARGUMENT.toString()),
              "Field.ARGUMENT toString");
        check(MessageFormat.Field.ARGUMENT == MessageFormat.Field.ARGUMENT, "constant identity");
    }

    static void testFormatToCharacterIteratorContract() {
        MessageFormat m = new MessageFormat("{0,number,0.0}", Locale.US);
        AttributedCharacterIterator it = m.formatToCharacterIterator(new Object[]{3.14});
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("3.1".equals(sb.toString()) || "3.14".equals(sb.toString()),
              "iterator text is the formatted number");
        it.first();
        check(it.getAttributes().containsKey(MessageFormat.Field.ARGUMENT),
              "ARGUMENT attribute present");
        check(it.getAttributes().containsKey(java.text.NumberFormat.Field.INTEGER),
              "nested number INTEGER attribute present");
    }
    public static void main(String[] args) throws Exception {
        testTypedSubformatsContract();
        testQuotingRulesContract();
        testArgumentIndexContract();
        testIllegalPatternsContract();
        testParseContract();
        testLocaleSwitchingContract();
        testFormatsAccessContract();
        testStaticAndObjectFormatContract();
        testFieldConstantsContract();
        testFormatToCharacterIteratorContract();
        testWhiteBox();
        testBasicSubstitution();
        testConstructorsAndExceptions();
        testSubformats();
        testExplicitFormats();
        testParse();
        testPatternAndLocale();
        testIteratorEqualsClone();
        testBoundaryValues();
        testSetFormatAndParseObject();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
