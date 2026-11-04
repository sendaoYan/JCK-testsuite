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

    public static void main(String[] args) throws Exception {
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
