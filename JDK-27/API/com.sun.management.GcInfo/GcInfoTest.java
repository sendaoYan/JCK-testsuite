/*
 * @test
 * @summary com.sun.management.GcInfo conformance: white-box structure + black-box behavior.
 * @run main GcInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import javax.management.openmbean.CompositeData;

public class GcInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.GcInfo");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.management.openmbean.CompositeData"), "CompositeData");
        check(interfaces(c).contains("javax.management.openmbean.CompositeDataView"), "CompositeDataView");
        Set<String> m = methodNames(c);
        String[] names = {"getId","getStartTime","getEndTime","getDuration",
            "getMemoryUsageBeforeGc","getMemoryUsageAfterGc","from","toCompositeData",
            "containsKey","containsValue","get","getAll","getCompositeType",
            "equals","hashCode","toString","values"};
        for (String n : names) check(m.contains(n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // from(null) == null.
        check(com.sun.management.GcInfo.from(null) == null, "from(null) == null");

        // Obtain a real GcInfo from a HotSpot GC MXBean. getLastGcInfo() may
        // legitimately be null before any GC has been recorded, so tolerate that.
        System.gc();
        com.sun.management.GcInfo gi = null;
        for (java.lang.management.GarbageCollectorMXBean b :
                ManagementFactory.getGarbageCollectorMXBeans()) {
            if (b instanceof com.sun.management.GarbageCollectorMXBean) {
                com.sun.management.GcInfo x =
                    ((com.sun.management.GarbageCollectorMXBean) b).getLastGcInfo();
                if (x != null) { gi = x; break; }
            }
        }
        if (gi == null) {
            System.out.println("NOTE: no GcInfo available from getLastGcInfo(); tolerated.");
            return;
        }

        // Basic identity/time invariants.
        check(gi.getId() >= 0, "id >= 0");
        check(gi.getStartTime() >= 0, "startTime >= 0");
        check(gi.getEndTime() >= gi.getStartTime(), "endTime >= startTime");
        check(gi.getDuration() >= 0, "duration >= 0");
        check(gi.getDuration() == gi.getEndTime() - gi.getStartTime(), "duration == end - start");

        // Memory-usage maps: non-null, mutually consistent keys, non-null values.
        Map<String, MemoryUsage> before = gi.getMemoryUsageBeforeGc();
        Map<String, MemoryUsage> after = gi.getMemoryUsageAfterGc();
        check(before != null, "memoryUsageBeforeGc non-null");
        check(after != null, "memoryUsageAfterGc non-null");
        List<String> keys = new ArrayList<>(before.keySet());
        Collections.sort(keys);
        check(keys.size() >= 0, "before map size >= 0");
        for (String k : keys) {
            check(k != null, "before key non-null");
            MemoryUsage b = before.get(k);
            check(b != null, "before value non-null for " + k);
            check(after.get(k) != null, "after value exists for " + k);
        }

        // CompositeData view.
        check(gi.getCompositeType() != null, "getCompositeType non-null");
        check(gi.containsKey("id"), "containsKey(id)");
        check(gi.get("id") instanceof Long, "get(id) is Long");
        Object[] all = gi.getAll(new String[] {"id", "startTime", "endTime", "duration"});
        check(all != null && all.length == 4, "getAll length");
        check(gi.toString() != null, "toString non-null");
        check(gi.equals(gi), "equals self");
        check(gi.hashCode() == gi.hashCode(), "hashCode stable");

        // CompositeDataView round-trip.
        CompositeData cd = gi.toCompositeData(null);
        check(cd != null, "toCompositeData non-null");
        com.sun.management.GcInfo back = com.sun.management.GcInfo.from(cd);
        check(back != null, "from non-null");
        check(back.getId() == gi.getId(), "round-trip id");
        check(back.getStartTime() == gi.getStartTime(), "round-trip startTime");
        check(back.getEndTime() == gi.getEndTime(), "round-trip endTime");
        check(back.getDuration() == gi.getDuration(), "round-trip duration");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
