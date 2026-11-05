/*
 * @test
 * @summary java.lang.management.MemoryNotificationInfo conformance: pool/usage/count.
 * @run main MemoryNotificationInfoTest
 */

import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryUsage;

public class MemoryNotificationInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryNotificationInfo() {
        MemoryUsage mu = new MemoryUsage(100, 50, 100, 100);
        MemoryNotificationInfo info = new MemoryNotificationInfo("pool", mu, 7);
        check("pool".equals(info.getPoolName()), "getPoolName");
        check(info.getUsage().getUsed() == 50, "getUsage");
        check(info.getCount() == 7, "getCount");
        check(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED != null,
              "MEMORY_THRESHOLD_EXCEEDED");
        check(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED != null,
              "MEMORY_COLLECTION_THRESHOLD_EXCEEDED");
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

    static void testDeepConstructorNullMatrix() {
        MemoryUsage usage = new MemoryUsage(10, 20, 30, 40);
        expectNPE(new Runnable() { public void run() { new MemoryNotificationInfo(null, usage, 1); } },
                  "poolName=null", "poolName");
        expectNPE(new Runnable() { public void run() { new MemoryNotificationInfo("p", null, 1); } },
                  "usage=null", "usage");
        // count is stored as given: negative and zero are accepted (lenient constructor)
        MemoryUsage u2 = new MemoryUsage(1, 1, 1, 1);
        check(new MemoryNotificationInfo("p", u2, -1).getCount() == -1, "count=-1 accepted, round-trips");
        check(new MemoryNotificationInfo("p", u2, 0).getCount() == 0, "count=0 round-trips");
        check(new MemoryNotificationInfo("p", u2, Long.MAX_VALUE).getCount() == Long.MAX_VALUE,
              "count=Long.MAX_VALUE round-trips");
        MemoryNotificationInfo info = new MemoryNotificationInfo("Eden", u2, 7);
        check(info.getUsage() == u2, "getUsage returns the same instance (no copy)");
    }

    static void testDeepNotificationConstants() {
        check("java.management.memory.threshold.exceeded"
                .equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED),
              "MEMORY_THRESHOLD_EXCEEDED exact type string");
        check("java.management.memory.collection.threshold.exceeded"
                .equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED),
              "MEMORY_COLLECTION_THRESHOLD_EXCEEDED exact type string");
    }

    static void testDeepFromCompositeData() throws Exception {
        check(MemoryNotificationInfo.from(null) == null, "from(null) returns null (no NPE)");
        javax.management.openmbean.CompositeType usageType = new javax.management.openmbean.CompositeType(
                "MemoryUsage", "MemoryUsage",
                new String[]{"init", "used", "committed", "max"},
                new String[]{"init", "used", "committed", "max"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG,
                        javax.management.openmbean.SimpleType.LONG});
        javax.management.openmbean.CompositeData usageCd = new javax.management.openmbean.CompositeDataSupport(
                usageType,
                new String[]{"init", "used", "committed", "max"},
                new Object[]{Long.valueOf(10), Long.valueOf(20), Long.valueOf(30), Long.valueOf(40)});
        javax.management.openmbean.CompositeType infoType = new javax.management.openmbean.CompositeType(
                "MemoryNotificationInfo", "MemoryNotificationInfo",
                new String[]{"poolName", "usage", "count"},
                new String[]{"poolName", "usage", "count"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.STRING,
                        usageType,
                        javax.management.openmbean.SimpleType.LONG});
        javax.management.openmbean.CompositeData cd = new javax.management.openmbean.CompositeDataSupport(
                infoType,
                new String[]{"poolName", "usage", "count"},
                new Object[]{"Eden", usageCd, Long.valueOf(5)});
        MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
        check(info != null, "from(valid CompositeData) non-null");
        check("Eden".equals(info.getPoolName()), "from() poolName round-trip");
        check(info.getCount() == 5, "from() count round-trip");
        check(info.getUsage().getUsed() == 20 && info.getUsage().getMax() == 40,
              "from() nested MemoryUsage round-trip");
        // wrong item type for the nested usage -> documented IAE
        javax.management.openmbean.CompositeType badType = new javax.management.openmbean.CompositeType(
                "MemoryNotificationInfo", "MemoryNotificationInfo",
                new String[]{"poolName", "usage", "count"},
                new String[]{"poolName", "usage", "count"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.LONG});
        final javax.management.openmbean.CompositeData bad = new javax.management.openmbean.CompositeDataSupport(
                badType,
                new String[]{"poolName", "usage", "count"},
                new Object[]{"Eden", "oops", Long.valueOf(5)});
        expectIAE(new Runnable() { public void run() { MemoryNotificationInfo.from(bad); } },
                  "from(string usage item)", "Unexpected composite type");
    }

    public static void main(String[] args) throws Exception {
        testMemoryNotificationInfo();
        testDeepConstructorNullMatrix();
        testDeepNotificationConstants();
        testDeepFromCompositeData();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
