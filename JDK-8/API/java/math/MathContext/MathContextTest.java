/*
 * @test
 * @summary java.math.MathContext API conformance: full contract coverage.
 *          constants (UNLIMITED/DECIMAL32/64/128), constructors (int /
 *          int+RoundingMode / String), getPrecision/getRoundingMode, equals/
 *          hashCode/toString, serialization round-trip + serialVersionUID,
 *          and exact exception matrix (IllegalArgumentException / NPE).
 * @run main MathContextTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static MathContext roundTrip(MathContext v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (MathContext) ois.readObject();
    }

    static void testConstants() {
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision");
        check(MathContext.UNLIMITED.getRoundingMode() == RoundingMode.HALF_UP, "UNLIMITED mode");
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision");
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL32 mode");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision");
        check(MathContext.DECIMAL64.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL64 mode");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision");
        check(MathContext.DECIMAL128.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL128 mode");
    }

    static void testConstructors() {
        check(new MathContext(10).getPrecision() == 10, "(int) precision");
        check(new MathContext(10).getRoundingMode() == RoundingMode.HALF_UP, "(int) default mode");
        check(new MathContext(0).getPrecision() == 0, "(int) zero precision allowed");
        MathContext mc = new MathContext(10, RoundingMode.HALF_DOWN);
        check(mc.getPrecision() == 10 && mc.getRoundingMode() == RoundingMode.HALF_DOWN,
                "(int, RoundingMode)");
        MathContext s = new MathContext("precision=5 roundingMode=HALF_DOWN");
        check(s.getPrecision() == 5 && s.getRoundingMode() == RoundingMode.HALF_DOWN,
                "String constructor");
        check(new MathContext("precision=3 roundingMode=UP").getPrecision() == 3,
                "String precision");
    }

    static void testEqualsHashCodeToString() {
        check(new MathContext(10).equals(new MathContext(10)), "equals");
        check(!new MathContext(10).equals(new MathContext(11)), "equals diff precision");
        check(!new MathContext(10, RoundingMode.UP).equals(new MathContext(10, RoundingMode.DOWN)),
                "equals diff mode");
        check(new MathContext(10).hashCode() == new MathContext(10).hashCode(), "hashCode");
        check(new MathContext(10).equals(null) == false, "equals null");
        check(new MathContext(10).toString().equals("precision=10 roundingMode=HALF_UP"), "toString");
        check(new MathContext(5, RoundingMode.HALF_DOWN).toString()
                .equals("precision=5 roundingMode=HALF_DOWN"), "toString mode");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(MathContext.class).getSerialVersionUID() == 5579720004786848255L,
                "serialVersionUID");
        MathContext[] vals = { MathContext.UNLIMITED, MathContext.DECIMAL32,
                new MathContext(17, RoundingMode.DOWN) };
        for (MathContext v : vals) {
            MathContext r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(IllegalArgumentException.class, () -> new MathContext(-1), "negative precision");
        checkThrows(NullPointerException.class, () -> new MathContext(10, null), "null RoundingMode");
        checkThrows(NullPointerException.class, () -> new MathContext((String) null), "null String");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("precision=5"),
                "String missing roundingMode");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("precision=-1 roundingMode=UP"),
                "String negative precision");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("garbage"), "bad String");
    }

    public static void main(String[] args) throws Exception {
        testConstants();
        testConstructors();
        testEqualsHashCodeToString();
        testSerialization();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
