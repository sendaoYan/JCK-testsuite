/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer conformance: XML limit analyzer (internal).
 * @run main XMLLimitAnalyzerTest
 */


public class XMLLimitAnalyzerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    static Object en(Class<?> c, String name) {
        return java.lang.Enum.valueOf((Class) c, name);
    }

    public static void main(String[] args) throws Exception {
        jdk.xml.internal.XMLLimitAnalyzer a = new jdk.xml.internal.XMLLimitAnalyzer();
        check(a != null, "constructed");
        check(!a.isTracking("unknown"), "not tracking unknown");

        // ---- reflection surface for the deepened contract ----
        Class<?> laC = Class.forName("jdk.xml.internal.XMLLimitAnalyzer");
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager");
        java.lang.reflect.Method addValue = laC.getMethod("addValue", limC, String.class, int.class);
        java.lang.reflect.Method getValue = laC.getMethod("getValue", limC);
        java.lang.reflect.Method getTotalValue = laC.getMethod("getTotalValue", limC);
        java.lang.reflect.Method getValueByIndex = laC.getMethod("getValueByIndex", int.class);
        Object expL = en(limC, "ENTITY_EXPANSION_LIMIT");
        Object occL = en(limC, "MAX_OCCUR_NODE_LIMIT");
        Object attL = en(limC, "ELEMENT_ATTRIBUTE_LIMIT");
        Object totL = en(limC, "TOTAL_ENTITY_SIZE_LIMIT");
        Object genL = en(limC, "GENERAL_ENTITY_SIZE_LIMIT");
        Object parL = en(limC, "PARAMETER_ENTITY_SIZE_LIMIT");
        Object dptL = en(limC, "MAX_ELEMENT_DEPTH_LIMIT");
        Object namL = en(limC, "MAX_NAME_LIMIT");
        Object repL = en(limC, "ENTITY_REPLACEMENT_LIMIT");

        // ---- fresh analyzer: all counters zero ----
        Object fresh = laC.getConstructor().newInstance();
        for (int i = 0; i < 12; i++) {
            check(((Integer) laC.getMethod("getValue", int.class).invoke(fresh, i)).intValue() == 0, "fresh values[" + i + "]==0");
            check(((Integer) getTotalValue.invoke(fresh, limC.getEnumConstants()[i])).intValue() == 0, "fresh totals[" + i + "]==0");
        }

        // ---- accumulator limits: addValue only accumulates totalValue ----
        Object acc = laC.getConstructor().newInstance();
        addValue.invoke(acc, expL, "doc", 3);
        addValue.invoke(acc, expL, "doc", 4);
        check(((Integer) getTotalValue.invoke(acc, expL)).intValue() == 7, "expansion totals accumulate 3+4=7");
        check(((Integer) getValueByIndex.invoke(acc, ((java.lang.Enum<?>) expL).ordinal())).intValue() == 0, "expansion per-name max stays 0");
        addValue.invoke(acc, occL, "schema", 10);
        addValue.invoke(acc, occL, "schema", 5);
        check(((Integer) getTotalValue.invoke(acc, occL)).intValue() == 15, "maxOccur totals accumulate 15");
        addValue.invoke(acc, attL, "elem", 2);
        check(((Integer) getTotalValue.invoke(acc, attL)).intValue() == 2, "element attribute totals accumulate");

        // ---- replacement limit: getValue returns the accumulated total (special case) ----
        Object rep = laC.getConstructor().newInstance();
        addValue.invoke(rep, repL, "node", 6);
        addValue.invoke(rep, repL, "node", 8);
        check(((Integer) getValue.invoke(rep, repL)).intValue() == 14, "ENTITY_REPLACEMENT getValue == totalValue");
        check(((Integer) getValueByIndex.invoke(rep, ((java.lang.Enum<?>) repL).ordinal())).intValue() == 0, "replacement raw values slot untouched");

        // ---- last-value limits: value overwrites both slots ----
        Object dep = laC.getConstructor().newInstance();
        addValue.invoke(dep, dptL, "depth", 5);
        check(((Integer) getValue.invoke(dep, dptL)).intValue() == 5, "depth first value 5");
        check(((Integer) getTotalValue.invoke(dep, dptL)).intValue() == 5, "depth first total 5");
        addValue.invoke(dep, dptL, "depth", 3);
        check(((Integer) getValue.invoke(dep, dptL)).intValue() == 3, "depth second value overwrites to 3");
        check(((Integer) getTotalValue.invoke(dep, dptL)).intValue() == 3, "depth total overwritten to 3");
        addValue.invoke(dep, namL, "name", 12);
        check(((Integer) getValue.invoke(dep, namL)).intValue() == 12, "max name value tracked");

        // ---- cache limits: same entity accumulates, cross-entity max recorded, total feeds TOTAL_ENTITY_SIZE ----
        Object gen = laC.getConstructor().newInstance();
        addValue.invoke(gen, genL, "e1", 3);
        addValue.invoke(gen, genL, "e1", 4);
        check(((Integer) getValue.invoke(gen, genL)).intValue() == 7, "entity e1 accumulated size 7");
        addValue.invoke(gen, genL, "e2", 2);
        check(((Integer) getValue.invoke(gen, genL)).intValue() == 7, "smaller entity e2 does not lower the max");
        check(((Integer) getTotalValue.invoke(gen, totL)).intValue() == 9, "general entity sizes feed TOTAL_ENTITY_SIZE (3+4+2)");
        check(((Integer) getTotalValue.invoke(gen, genL)).intValue() == 0, "general entity own total slot stays 0");
        addValue.invoke(gen, parL, "p1", 10);
        check(((Integer) getTotalValue.invoke(gen, totL)).intValue() == 19, "parameter entity sizes also feed TOTAL_ENTITY_SIZE");
        check(((Integer) getValue.invoke(gen, parL)).intValue() == 10, "parameter entity size tracked");

        // ---- startEntity / isTracking / endEntity ----
        Object trk = laC.getConstructor().newInstance();
        check(!((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "x")).booleanValue(), "nothing tracked initially");
        laC.getMethod("startEntity", String.class).invoke(trk, "xxe");
        check(((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "xxe")).booleanValue(), "tracking started entity");
        check(!((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "other")).booleanValue(), "different entity not tracked");
        check(!((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, (Object) null)).booleanValue(), "null entity not tracked");
        laC.getMethod("endEntity", limC, String.class).invoke(trk, genL, "e1");
        check(!((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "xxe")).booleanValue(), "tracking cleared after endEntity");
        laC.getMethod("startEntity", String.class).invoke(trk, "second");
        check(((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "second")).booleanValue(), "second entity tracked after restart");
        laC.getMethod("endEntity", limC, String.class).invoke(trk, genL, "second");
        check(!((Boolean) laC.getMethod("isTracking", String.class).invoke(trk, "second")).booleanValue(), "second tracking cleared");
        // endEntity also drops the entity from the size cache
        laC.getMethod("endEntity", limC, String.class).invoke(gen, genL, "e1");
        addValue.invoke(gen, genL, "e1", 5);
        check(((Integer) getValue.invoke(gen, genL)).intValue() == 7, "cache re-accumulates from 0 after endEntity removal (5<7 keeps max)");

        // ---- reset semantics per limit ----
        Object rst = laC.getConstructor().newInstance();
        addValue.invoke(rst, totL, "t", 100);
        addValue.invoke(rst, genL, "g", 50);
        laC.getMethod("reset", limC).invoke(rst, totL);
        check(((Integer) getTotalValue.invoke(rst, totL)).intValue() == 0, "reset TOTAL_ENTITY_SIZE clears total");
        laC.getMethod("reset", limC).invoke(rst, genL);
        check(((Integer) getValue.invoke(rst, genL)).intValue() == 0, "reset GENERAL clears value");
        check(((Integer) getTotalValue.invoke(rst, genL)).intValue() == 0, "reset GENERAL clears its total");
        laC.getMethod("reset", limC).invoke(rst, expL);
        check(true, "reset on other limits is a no-op, no throw");

        // ---- debugPrint: emits the report table without throwing ----
        Object sm = smC.getConstructor(boolean.class).newInstance(true);
        Object prn = laC.getConstructor().newInstance();
        addValue.invoke(prn, expL, "doc", 9);
        laC.getMethod("debugPrint", smC).invoke(prn, sm);
        check(true, "debugPrint no throw");
        laC.getMethod("debugPrint", smC).invoke(laC.getConstructor().newInstance(), smC.getConstructor(boolean.class).newInstance(false));
        check(true, "debugPrint on empty analyzer no throw");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
