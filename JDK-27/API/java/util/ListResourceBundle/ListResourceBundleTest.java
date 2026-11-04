/*
 * @test
 * @summary java.util.ListResourceBundle conformance: white-box structure plus black-box behavior
 *          of getContents/getString/getObject/getStringArray/getKeys/containsKey/keySet.
 * @run main ListResourceBundleTest
 */

import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.Set;

public class ListResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static class LRB extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                { "k", "v" },
                { "n", Integer.valueOf(3) },
                { "arr", new String[] { "a", "b" } },
            };
        }
    }

    static class NullContentsLRB extends ListResourceBundle {
        protected Object[][] getContents() {
            return null;
        }
    }

    static class NullKeyLRB extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] { { null, "v" } };
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ListResourceBundle");
        check(!c.isInterface(), "ListResourceBundle is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ListResourceBundle is abstract");
        check(java.util.ResourceBundle.class.isAssignableFrom(c), "ListResourceBundle extends ResourceBundle");
        c.getDeclaredMethod("getContents");
        c.getMethod("handleGetObject", String.class);
        c.getMethod("getKeys");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ListResourceBundle b = new LRB();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(3).equals(b.getObject("n")), "getObject");
        String[] arr = b.getStringArray("arr");
        check(arr.length == 2 && "a".equals(arr[0]) && "b".equals(arr[1]), "getStringArray");
        check(b.containsKey("arr"), "containsKey");
        check(b.getKeys().hasMoreElements(), "getKeys has elements");

        Set<String> keys = b.keySet();
        check(keys.size() == 3 && keys.contains("k") && keys.contains("n") && keys.contains("arr"),
              "keySet has all keys");

        assertThrows(MissingResourceException.class,
                () -> b.getString("missing"), "getString missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getObject("missing"), "getObject missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getStringArray("missing"), "getStringArray missing throws MissingResourceException");

        // Lookup is loaded lazily on first access; null contents or a null
        // key/value pair therefore NPEs on access, not at construction.
        assertThrows(NullPointerException.class,
                () -> new NullContentsLRB().getKeys(), "getContents() null -> NPE on access");
        assertThrows(NullPointerException.class,
                () -> new NullKeyLRB().getString("k"), "null key -> NPE on access");
    }


    // ===================== 契约深化：父链联合 keySet/handleKeySet 范围/getKeys 全枚举 =====================
    static class BaseBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] { {"pk", "pv"}, {"both", "base"} };
        }
    }

    static class DerivedBundle extends BaseBundle {
        protected Object[][] getContents() {
            return new Object[][] { {"ck", "cv"}, {"both", "derived"} };
        }
        Object[][] ownContents() { return super.getContents(); }
    }

    static void testListBundleContracts() {
        DerivedBundle d = new DerivedBundle();
        check("cv".equals(d.getString("ck")), "derived key");
        check("derived".equals(d.getString("both")), "derived shadows base in contents");
        check(d.getContents() != null && d.getContents().length == 2,
              "getContents returns derived array");
        Object[][] own = d.ownContents();
        boolean baseShape = own.length == 2;
        for (Object[] pair : own) if (!"pv".equals(pair[1]) && !"base".equals(pair[1])) baseShape = false;
        check(baseShape, "super.getContents from subclass returns the BASE implementation (non-virtual super call)");
        java.util.Set<String> keys = d.keySet();
        check(keys.contains("ck"), "keySet contains derived key");
        check(!keys.contains("zz"), "keySet excludes unknown key");
        java.util.Enumeration<String> en = d.getKeys();
        int n = 0;
        while (en.hasMoreElements()) { en.nextElement(); n++; }
        check(n == 2, "getKeys enumerates exactly the contents entries");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testListBundleContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
