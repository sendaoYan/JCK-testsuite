/*
 * @test
 * @summary javax.management.openmbean.CompositeDataView conformance: white-box structure + light black-box behavior.
 * @run main CompositeDataViewTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeDataView;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeDataViewTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataView");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "toCompositeData"), "has toCompositeData");
        check(mm.length == 1, "single method");
    }

    // ---- Part 2: black-box (light behavior via anonymous implementation) ----
    static void testBlackBox() throws Exception {
        final CompositeType ct = new CompositeType("CT", "d",
            new String[]{"name"}, new String[]{"nd"}, new OpenType[]{SimpleType.STRING});
        final CompositeData expected = new CompositeDataSupport(ct, new String[]{"name"}, new Object[]{"n"});

        CompositeDataView view = new CompositeDataView() {
            public CompositeData toCompositeData(CompositeType type) {
                check(type.equals(ct), "type passed to toCompositeData");
                return expected;
            }
        };
        check(view.toCompositeData(ct).equals(expected), "toCompositeData returns value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
