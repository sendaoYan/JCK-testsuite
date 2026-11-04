/*
 * @test
 * @summary java.text.FieldPosition conformance: white-box structure + black-box behavior.
 * @run main FieldPositionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public class FieldPositionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.FieldPosition");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, non-final class");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 3, "three public constructors (int, Format.Field, Format.Field+int)");
        for (Constructor<?> ct : ctors)
            check(Modifier.isPublic(ct.getModifiers()), "constructor public: " + ct);

        check(c.getMethod("getField").getReturnType() == int.class, "getField() -> int");
        check(c.getMethod("getFieldAttribute").getReturnType() == Format.Field.class,
                "getFieldAttribute() -> Format.Field");
        check(c.getMethod("getBeginIndex").getReturnType() == int.class, "getBeginIndex() -> int");
        check(c.getMethod("getEndIndex").getReturnType() == int.class, "getEndIndex() -> int");
        check(c.getMethod("setBeginIndex", int.class).getReturnType() == void.class, "setBeginIndex(int)");
        check(c.getMethod("setEndIndex", int.class).getReturnType() == void.class, "setEndIndex(int)");
        check(c.getMethod("equals", Object.class).getReturnType() == boolean.class, "equals(Object)");
        check(c.getMethod("hashCode").getReturnType() == int.class, "hashCode()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. int constructor + getters/setters
        FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);
        check(fp.getField() == NumberFormat.INTEGER_FIELD, "int ctor: getField");
        check(fp.getFieldAttribute() == null, "int ctor: attribute null");
        check(fp.getBeginIndex() == 0 && fp.getEndIndex() == 0, "indexes default to 0");
        fp.setBeginIndex(4);
        fp.setEndIndex(9);
        check(fp.getBeginIndex() == 4, "setBeginIndex/getBeginIndex");
        check(fp.getEndIndex() == 9, "setEndIndex/getEndIndex");
        fp.setBeginIndex(7);
        fp.setEndIndex(12);
        check(fp.getBeginIndex() == 7 && fp.getEndIndex() == 12, "setters overwrite");

        // 2. equals/hashCode across all four state components
        FieldPosition a = new FieldPosition(3);
        FieldPosition b = new FieldPosition(3);
        check(a.equals(b) && b.equals(a), "equal for same field + default indexes");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        b.setBeginIndex(1);
        check(!a.equals(b), "beginIndex participates in equals");
        b.setBeginIndex(0);
        b.setEndIndex(2);
        check(!a.equals(b), "endIndex participates in equals");
        check(!a.equals(new FieldPosition(4)), "field id participates in equals");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("x"), "equals(non-FieldPosition) false");
        check(a.equals(a), "equals(self) true");

        // 3. attribute constructors
        FieldPosition fa = new FieldPosition(DateFormat.Field.ERA);
        check(fa.getFieldAttribute() == DateFormat.Field.ERA, "attribute ctor: getFieldAttribute");
        check(fa.getField() == -1, "attribute ctor: field id -1 (FIELD_UNDEFINED)");
        FieldPosition fc = new FieldPosition(NumberFormat.Field.INTEGER, NumberFormat.INTEGER_FIELD);
        check(fc.getFieldAttribute() == NumberFormat.Field.INTEGER, "combined ctor: attribute kept");
        check(fc.getField() == NumberFormat.INTEGER_FIELD, "combined ctor: field id kept");
        check(!fa.equals(fc), "attribute vs combined differ");
        FieldPosition fa2 = new FieldPosition(DateFormat.Field.ERA);
        check(fa.equals(fa2) && fa.hashCode() == fa2.hashCode(), "attribute-based equals/hashCode");
        fa2.setBeginIndex(5);
        check(!fa.equals(fa2), "attribute equal broken by beginIndex");

        // 4. toString formats all four fields
        FieldPosition t = new FieldPosition(1);
        t.setBeginIndex(2);
        t.setEndIndex(5);
        check("java.text.FieldPosition[field=1,attribute=null,beginIndex=2,endIndex=5]"
                .equals(t.toString()), "toString exact for int ctor");
        FieldPosition ta = new FieldPosition(DateFormat.Field.ERA);
        check(ta.toString().contains("attribute=java.text.DateFormat$Field(era)"),
                "toString shows attribute name");
        check(ta.toString().contains("field=-1"), "toString shows FIELD_UNDEFINED");

        // 5. real usage: DecimalFormat reports integer/fraction spans into FieldPosition
        NumberFormat nf = DecimalFormat.getNumberInstance(Locale.US);
        String s = nf.format(1234567.89);
        FieldPosition ip = new FieldPosition(NumberFormat.INTEGER_FIELD);
        nf.format(1234567.89, new StringBuffer(), ip);
        int dot = s.indexOf('.');
        check(ip.getBeginIndex() == 0 && ip.getEndIndex() == dot,
                "INTEGER_FIELD span matches formatted integer part: " + s);
        FieldPosition ap = new FieldPosition(NumberFormat.Field.FRACTION);
        nf.format(1234567.89, new StringBuffer(), ap);
        check(ap.getBeginIndex() == dot + 1 && ap.getEndIndex() == s.length(),
                "FRACTION attribute span matches fraction part");
        check(ap.getFieldAttribute() == NumberFormat.Field.FRACTION,
                "getFieldAttribute returns the same attribute instance after format");

        // ---- boundary/equivalence additions (M2) ----
        FieldPosition fpb = new FieldPosition(NumberFormat.INTEGER_FIELD);
        fpb.setBeginIndex(-1);
        fpb.setEndIndex(-1);
        check(fpb.getBeginIndex() == -1 && fpb.getEndIndex() == -1, "negative indexes allowed");

        FieldPosition zero = new FieldPosition(NumberFormat.INTEGER_FIELD);
        check(zero.getBeginIndex() == 0 && zero.getEndIndex() == 0, "indexes default to 0");

        FieldPosition fneg = new FieldPosition(-1);
        check(fneg.getField() == -1, "int ctor field -1 allowed");

        FieldPosition na = new FieldPosition(-1);
        na.setBeginIndex(2);
        na.setEndIndex(3);
        FieldPosition nb = new FieldPosition(-1);
        nb.setBeginIndex(2);
        nb.setEndIndex(3);
        check(na.equals(nb) && na.hashCode() == nb.hashCode(), "field -1 equals/hashCode");
        nb.setBeginIndex(4);
        check(!na.equals(nb), "beginIndex boundary distinguishes equals");

        check("java.text.FieldPosition[field=-1,attribute=null,beginIndex=2,endIndex=3]"
                .equals(na.toString()), "toString field -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
