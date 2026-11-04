/*
 * @test
 * @summary java.text.Format conformance: white-box structure + black-box behavior.
 * @run main FormatTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

public class FormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    /** Minimal Format subclass that records the subclass-contract interactions. */
    public static class UpperFormat extends Format {
        boolean formatCalled;
        boolean parseCalled;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            formatCalled = true;
            String s = String.valueOf(obj).toUpperCase(Locale.ROOT);
            int start = toAppendTo.length();
            toAppendTo.append(s);
            pos.setBeginIndex(start);
            pos.setEndIndex(toAppendTo.length());
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition status) {
            parseCalled = true;
            int idx = status.getIndex();
            if (source.startsWith("hi", idx)) {
                status.setIndex(idx + 2);
                return "HI";
            }
            status.setErrorIndex(idx);
            return null;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.Format");
        check(Modifier.isAbstract(c.getModifiers()), "Format is abstract");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
                "single protected constructor");

        Method fmt = c.getMethod("format", Object.class, StringBuffer.class, FieldPosition.class);
        check(Modifier.isAbstract(fmt.getModifiers()),
                "format(Object,StringBuffer,FieldPosition) abstract");
        check(fmt.getReturnType() == StringBuffer.class, "format(...) returns StringBuffer");

        Method fmtObj = c.getMethod("format", Object.class);
        check(Modifier.isFinal(fmtObj.getModifiers()), "format(Object) is final");
        check(fmtObj.getReturnType() == String.class, "format(Object) returns String");

        Method parse2 = c.getMethod("parseObject", String.class, ParsePosition.class);
        check(Modifier.isAbstract(parse2.getModifiers()), "parseObject(String,ParsePosition) abstract");
        check(parse2.getReturnType() == Object.class, "parseObject(...) returns Object");

        Method parse1 = c.getMethod("parseObject", String.class);
        check(!Modifier.isAbstract(parse1.getModifiers()), "parseObject(String) concrete");
        boolean throwsParse = false;
        for (Class<?> ex : parse1.getExceptionTypes())
            if (ex == ParseException.class) throwsParse = true;
        check(throwsParse, "parseObject(String) declares ParseException");

        Method fci = c.getMethod("formatToCharacterIterator", Object.class);
        check(fci.getReturnType() == AttributedCharacterIterator.class,
                "formatToCharacterIterator returns AttributedCharacterIterator");
        check(!Modifier.isAbstract(fci.getModifiers()), "formatToCharacterIterator has default body");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. final format(Object) delegates to format(Object,StringBuffer,FieldPosition)
        UpperFormat uf = new UpperFormat();
        check("HELLO".equals(uf.format("hello")), "format(Object) returns buffer text");
        check(uf.formatCalled, "format(Object) delegated to subclass format(...)");

        // 2. format(...) appends to caller's buffer and honors FieldPosition
        UpperFormat uf2 = new UpperFormat();
        FieldPosition fp = new FieldPosition(0);
        StringBuffer sb = new StringBuffer(">");
        StringBuffer ret = uf2.format("ab", sb, fp);
        check(ret == sb, "format(...) returns the caller's buffer");
        check(">AB".contentEquals(sb), "format(...) appends (does not replace) buffer content");
        check(fp.getBeginIndex() == 1 && fp.getEndIndex() == 3,
                "FieldPosition begin/end recorded during format");

        // 3. parseObject(String, ParsePosition): success advances index, failure sets errorIndex
        ParsePosition ok = new ParsePosition(0);
        check("HI".equals(uf.parseObject("hi there", ok)), "parseObject success");
        check(ok.getIndex() == 2 && ok.getErrorIndex() == -1, "success advanced index");
        ParsePosition bad = new ParsePosition(0);
        check(uf.parseObject("xx", bad) == null, "parseObject failure returns null");
        check(bad.getErrorIndex() == 0, "failure recorded errorIndex");
        ParsePosition mid = new ParsePosition(3);
        check("HI".equals(uf.parseObject("xxxhi", mid)), "parse resumes from current index");
        check(mid.getIndex() == 5, "resumed parse advanced index");
        check(uf.parseCalled, "subclass parseObject invoked");

        // 4. parseObject(String): success returns value, failure throws ParseException
        check("HI".equals(uf.parseObject("hi")), "parseObject(String) success");
        try {
            uf.parseObject("xx");
            throw new AssertionError("parseObject(String) failure: expected ParseException");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 0, "ParseException errorOffset from errorIndex");
        }

        // 5. clone() produces a distinct working copy
        UpperFormat copy = (UpperFormat) uf.clone();
        check(copy != uf && copy.getClass() == UpperFormat.class, "clone is a distinct same-class copy");
        check("HI".equals(copy.parseObject("hi", new ParsePosition(0))), "clone remains functional");

        // 6. default formatToCharacterIterator wraps format(Object) output in an iterator
        AttributedCharacterIterator aci = uf.formatToCharacterIterator("hi");
        check(aci != null, "formatToCharacterIterator returns an iterator");
        StringBuilder aciText = new StringBuilder();
        for (char c = aci.first(); c != java.text.CharacterIterator.DONE; c = aci.next())
            aciText.append(c);
        check("HI".contentEquals(aciText),
                "default formatToCharacterIterator iterates the formatted text");
        check(aci.getBeginIndex() == 0 && aci.getEndIndex() == 2,
                "formatToCharacterIterator bounds match formatted text length");
        check(aci.getAllAttributeKeys().isEmpty(),
                "default formatToCharacterIterator carries no attributes");

        // 7. real subclass through a Format reference (fixed Locale -> deterministic)
        Format num = DecimalFormat.getNumberInstance(Locale.US);
        check("1,234.5".equals(num.format(1234.5)), "DecimalFormat via Format reference");
        ParsePosition pp = new ParsePosition(0);
        Number n = (Number) num.parseObject("1,234.5xyz", pp);
        check(n != null && n.doubleValue() == 1234.5, "parseObject consumes numeric prefix");
        check(pp.getIndex() == 7, "numeric parse stopped at non-numeric char");
        ParsePosition fail = new ParsePosition(0);
        check(num.parseObject("xyz", fail) == null, "numeric parse failure returns null");
        check(fail.getErrorIndex() == 0, "numeric failure errorIndex");
        Number viaString = (Number) num.parseObject("2,500");
        check(viaString != null && viaString.doubleValue() == 2500.0, "parseObject(String) numeric");
        try {
            num.parseObject("zzz");
            throw new AssertionError("parseObject(String) numeric failure: expected ParseException");
        } catch (ParseException expected) {
            passed++;
        }

        // 8. Format is Serializable: round-trip a DecimalFormat and keep behavior
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(num);
        }
        try (ObjectInputStream ois =
                     new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Format revived = (Format) ois.readObject();
            check("1,234.5".equals(revived.format(1234.5)),
                    "serialization round-trip preserves formatting");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
