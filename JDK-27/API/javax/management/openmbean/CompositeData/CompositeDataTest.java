/*
 * @test
 * @summary javax.management.openmbean.CompositeData conformance: white-box structure + light black-box behavior.
 * @run main CompositeDataTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeData");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getCompositeType"), "has getCompositeType");
        check(has(mm, "get"), "has get");
        check(has(mm, "getAll"), "has getAll");
        check(has(mm, "containsKey"), "has containsKey");
        check(has(mm, "containsValue"), "has containsValue");
        check(has(mm, "values"), "has values");
        check(has(mm, "equals"), "has equals");
        check(has(mm, "hashCode"), "has hashCode");
        check(has(mm, "toString"), "has toString");
    }

    // ---- Part 2: black-box (light behavior via anonymous implementation) ----
    static void testBlackBox() throws Exception {
        final CompositeType ct = new CompositeType("CT", "d",
            new String[]{"name", "value"},
            new String[]{"nd", "vd"},
            new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});

        CompositeData cd = new CompositeData() {
            public CompositeType getCompositeType() { return ct; }
            public Object get(String key) { return "name".equals(key) ? "n" : ("value".equals(key) ? 5 : null); }
            public Object[] getAll(String[] keys) {
                if (keys == null || keys.length == 0) return new Object[0];
                Object[] r = new Object[keys.length];
                for (int i = 0; i < keys.length; i++) r[i] = get(keys[i]);
                return r;
            }
            public boolean containsKey(String key) { return "name".equals(key) || "value".equals(key); }
            public boolean containsValue(Object value) { return "n".equals(value) || Integer.valueOf(5).equals(value); }
            public Collection<?> values() { return Arrays.asList("n", 5); }
        };

        check(cd.getCompositeType().equals(ct), "getCompositeType");
        check(cd.get("name").equals("n"), "get name");
        check(cd.get("value").equals(5), "get value");
        check(cd.getAll(new String[]{"value", "name"}).length == 2, "getAll length");
        check(cd.containsKey("name") && !cd.containsKey("missing"), "containsKey");
        check(cd.containsValue("n") && cd.containsValue(5) && !cd.containsValue("x"), "containsValue");
        check(cd.values().size() == 2, "values size");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
