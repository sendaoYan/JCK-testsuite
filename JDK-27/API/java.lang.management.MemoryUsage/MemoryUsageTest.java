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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
