/*
 * @test
 * @summary java.lang.management.MemoryNotificationInfo conformance: white-box structure + black-box behavior.
 * @run main MemoryNotificationInfoTest
 */
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Modifier;

public class MemoryNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryNotificationInfo");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(String.class, MemoryUsage.class, long.class) != null,
              "constructor (String,MemoryUsage,long)");
        check(c.getMethod("getPoolName").getReturnType() == String.class, "getPoolName returns String");
        check(c.getMethod("getUsage").getReturnType() == MemoryUsage.class, "getUsage returns MemoryUsage");
        check(c.getMethod("getCount").getReturnType() == long.class, "getCount returns long");
        check("MEMORY_THRESHOLD_EXCEEDED".equals(c.getField("MEMORY_THRESHOLD_EXCEEDED").getName()),
              "MEMORY_THRESHOLD_EXCEEDED constant exists");
        check("MEMORY_COLLECTION_THRESHOLD_EXCEEDED".equals(c.getField("MEMORY_COLLECTION_THRESHOLD_EXCEEDED").getName()),
              "MEMORY_COLLECTION_THRESHOLD_EXCEEDED constant exists");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MemoryUsage usage = new MemoryUsage(10, 20, 30, 40);
        MemoryNotificationInfo info = new MemoryNotificationInfo("Eden", usage, 5L);
        check("Eden".equals(info.getPoolName()), "getPoolName");
        check(info.getUsage() == usage, "getUsage returns same instance");
        check(info.getCount() == 5L, "getCount");

        check(!MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.isEmpty(), "threshold constant non-empty");
        check(!MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.isEmpty(),
              "collection threshold constant non-empty");
        check(!MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED
                .equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED), "constants distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
