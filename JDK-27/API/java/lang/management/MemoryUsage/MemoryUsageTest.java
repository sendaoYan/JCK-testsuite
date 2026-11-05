/*
 * @test
 * @summary java.lang.management.MemoryUsage conformance: white-box structure + black-box behavior.
 * @run main MemoryUsageTest
 */
import java.lang.management.MemoryUsage;
import java.lang.reflect.Modifier;

public class MemoryUsageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryUsage");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(long.class, long.class, long.class, long.class) != null, "4-long constructor");
        check(c.getMethod("getInit").getReturnType() == long.class, "getInit returns long");
        check(c.getMethod("getUsed").getReturnType() == long.class, "getUsed returns long");
        check(c.getMethod("getCommitted").getReturnType() == long.class, "getCommitted returns long");
        check(c.getMethod("getMax").getReturnType() == long.class, "getMax returns long");
        check(c.getMethod("from", javax.management.openmbean.CompositeData.class).getReturnType() == MemoryUsage.class,
              "from returns MemoryUsage");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MemoryUsage mu = new MemoryUsage(10, 20, 30, 40);
        check(mu.getInit() == 10, "getInit");
        check(mu.getUsed() == 20, "getUsed");
        check(mu.getCommitted() == 30, "getCommitted");
        check(mu.getMax() == 40, "getMax");
        check(mu.toString().contains("10") && mu.toString().contains("20"), "toString contains values");

        MemoryUsage neg = new MemoryUsage(-1, 0, 0, -1);
        check(neg.getInit() == -1 && neg.getMax() == -1, "undefined init/max use -1");
        check(neg.getUsed() == 0 && neg.getCommitted() == 0, "zero used/committed");

        MemoryUsage z = new MemoryUsage(0, 0, 0, 0);
        check(z.getMax() == 0, "zero max");
    }


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepConstructorExceptionMatrix() {
        expectIAE(new Runnable() { public void run() { new MemoryUsage(-2, 0, 0, -1); } },
                  "init=-2", "init parameter", "-2");
        expectIAE(new Runnable() { public void run() { new MemoryUsage(0, -1, 0, -1); } },
                  "used=-1", "used parameter", "-1");
        expectIAE(new Runnable() { public void run() { new MemoryUsage(0, 0, -1, -1); } },
                  "committed=-1", "committed parameter", "-1");
        expectIAE(new Runnable() { public void run() { new MemoryUsage(0, 0, 0, -2); } },
                  "max=-2", "max parameter", "-2");
        expectIAE(new Runnable() { public void run() { new MemoryUsage(0, 50, 40, 100); } },
                  "used>committed", "committed", "40");
        // legal boundaries: -1 sentinel for init/max, all-zero, used==committed, Long.MAX_VALUE
        MemoryUsage sent = new MemoryUsage(-1, 0, 0, -1);
        check(sent.getInit() == -1 && sent.getMax() == -1, "init/max -1 sentinel legal");
        MemoryUsage zero = new MemoryUsage(0, 0, 0, 0);
        check(zero.getUsed() == 0 && zero.getCommitted() == 0, "all-zero legal");
        MemoryUsage big = new MemoryUsage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
        check(big.getUsed() == Long.MAX_VALUE, "Long.MAX_VALUE components legal");
        MemoryUsage eq = new MemoryUsage(10, 10, 10, 10);
        check(eq.getUsed() == eq.getCommitted(), "used==committed legal");
    }

    static void testDeepEqualsHashCodeToString() {
        // MemoryUsage does NOT override equals/hashCode: identity semantics by design
        MemoryUsage a = new MemoryUsage(100, 40, 80, 200);
        MemoryUsage b = new MemoryUsage(100, 40, 80, 200);
        check(a.equals(a), "equals self");
        check(!a.equals(b), "no value equals: identity semantics");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("x"), "equals(other type) false");
        String s = a.toString();
        check(s.contains("init") && s.contains("100"), "toString contains init label and value");
        check(s.contains("used") && s.contains("40"), "toString contains used value");
        check(s.contains("committed") && s.contains("80"), "toString contains committed value");
        check(s.contains("max") && s.contains("200"), "toString contains max value");
    }

    static void testDeepFromCompositeData() throws Exception {
        check(MemoryUsage.from(null) == null, "from(null) returns null (no NPE)");
        javax.management.openmbean.CompositeType good = new javax.management.openmbean.CompositeType(
                "MemoryUsage", "MemoryUsage",
                new String[]{"init", "used", "committed", "max"},
                new String[]{"init", "used", "committed", "max"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG});
        javax.management.openmbean.CompositeData cd = new javax.management.openmbean.CompositeDataSupport(
                good,
                new String[]{"init", "used", "committed", "max"},
                new Object[]{Long.valueOf(1000), Long.valueOf(400), Long.valueOf(800), Long.valueOf(1600)});
        MemoryUsage mu = MemoryUsage.from(cd);
        check(mu != null, "from(valid CompositeData) non-null");
        check(mu.getInit() == 1000 && mu.getUsed() == 400
                && mu.getCommitted() == 800 && mu.getMax() == 1600,
              "from(CompositeData) round-trips all four getters");
        // wrong item type -> documented IAE
        javax.management.openmbean.CompositeType strings = new javax.management.openmbean.CompositeType(
                "MemoryUsage", "MemoryUsage",
                new String[]{"init", "used", "committed", "max"},
                new String[]{"init", "used", "committed", "max"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.STRING});
        final javax.management.openmbean.CompositeData bad = new javax.management.openmbean.CompositeDataSupport(
                strings,
                new String[]{"init", "used", "committed", "max"},
                new Object[]{"1", "1", "1", "1"});
        expectIAE(new Runnable() { public void run() { MemoryUsage.from(bad); } },
                  "from(string-typed CompositeData)", "Unexpected composite type");
        // missing item -> same documented IAE
        javax.management.openmbean.CompositeType partial = new javax.management.openmbean.CompositeType(
                "MemoryUsage", "MemoryUsage",
                new String[]{"init", "committed", "max"},
                new String[]{"init", "committed", "max"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG});
        final javax.management.openmbean.CompositeData miss = new javax.management.openmbean.CompositeDataSupport(
                partial,
                new String[]{"init", "committed", "max"},
                new Object[]{Long.valueOf(1), Long.valueOf(1), Long.valueOf(1)});
        expectIAE(new Runnable() { public void run() { MemoryUsage.from(miss); } },
                  "from(missing 'used' item)", "Unexpected composite type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepConstructorExceptionMatrix();
        testDeepEqualsHashCodeToString();
        testDeepFromCompositeData();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
