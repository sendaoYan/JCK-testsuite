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

    static void testFormatDrivenPositionsContract() {
        DecimalFormat df = new DecimalFormat("#,##0.00", java.text.DecimalFormatSymbols.getInstance(Locale.US));
        java.text.FieldPosition frac = new java.text.FieldPosition(NumberFormat.Field.FRACTION);
        StringBuffer out = df.format(1234.56, new StringBuffer(), frac);
        check("1,234.56".equals(out.toString()), "formatted text");
        check(frac.getBeginIndex() == 6 && frac.getEndIndex() == 8, "FRACTION [6,8)");
        java.text.FieldPosition intg = new java.text.FieldPosition(NumberFormat.Field.INTEGER);
        df.format(-1234.56, new StringBuffer(), intg);
        check(intg.getBeginIndex() == 1 && intg.getEndIndex() == 6, "INTEGER [1,6) sign excluded");
        java.text.FieldPosition legacy = new java.text.FieldPosition(NumberFormat.INTEGER_FIELD);
        df.format(1234.56, new StringBuffer(), legacy);
        check(legacy.getField() == 0 && legacy.getBeginIndex() == 0 && legacy.getEndIndex() == 5,
              "legacy int field [0,5)");
        java.text.FieldPosition sign = new java.text.FieldPosition(NumberFormat.Field.SIGN);
        df.format(-1234.56, new StringBuffer(), sign);
        check(sign.getBeginIndex() == 0 && sign.getEndIndex() == 1, "SIGN [0,1)");
    }

    static void testEqualsHashCodeToStringContract() {
        java.text.FieldPosition a = new java.text.FieldPosition(NumberFormat.Field.INTEGER);
        java.text.FieldPosition b = new java.text.FieldPosition(NumberFormat.Field.INTEGER);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equal field attrs");
        check(!a.equals(new java.text.FieldPosition(NumberFormat.Field.FRACTION)),
              "different field attr not equal");
        check(!new java.text.FieldPosition(3).equals(new java.text.FieldPosition(4)),
              "different int field not equal");
        java.text.FieldPosition f = new java.text.FieldPosition(3);
        f.setBeginIndex(9); f.setEndIndex(11);
        check(f.getBeginIndex() == 9 && f.getEndIndex() == 11, "setBeginIndex/setEndIndex");
        check("java.text.FieldPosition[field=3,attribute=null,beginIndex=9,endIndex=11]"
              .equals(f.toString()), "toString exact format");
        check(new java.text.FieldPosition(0).getBeginIndex() == 0
              && new java.text.FieldPosition(0).getEndIndex() == 0, "default positions 0");
        check(java.text.FieldPosition.class.getAnnotation(Deprecated.class) == null,
              "not deprecated");
        check(!java.io.Serializable.class.isAssignableFrom(java.text.FieldPosition.class),
              "FieldPosition is not Serializable");
        check(new java.text.FieldPosition(7).getField() == 7, "int ctor getField");
        check(new java.text.FieldPosition(NumberFormat.Field.INTEGER).getFieldAttribute()
              == NumberFormat.Field.INTEGER, "field ctor getFieldAttribute identity");
    }
    public static void main(String[] args) throws Exception {
        testFormatDrivenPositionsContract();
        testEqualsHashCodeToStringContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
