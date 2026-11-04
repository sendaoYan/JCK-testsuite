/*
 * @test
 * @summary java.util.ResourceBundle conformance: white-box structure plus black-box behavior
 *          of getString/getObject/getStringArray/containsKey/keySet via a subclass, missing-key
 *          exceptions, and getBaseBundleName.
 * @run main ResourceBundleTest
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceBundleTest {
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

    static class SimpleBundle extends ResourceBundle {
        private final Map<String, Object> map = new HashMap<String, Object>();
        SimpleBundle() {
            map.put("k", "v");
            map.put("n", Integer.valueOf(7));
            map.put("arr", new String[] { "a", "b" });
        }
        protected Object handleGetObject(String key) {
            return map.get(key);
        }
        public Enumeration<String> getKeys() {
            return Collections.enumeration(map.keySet());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ResourceBundle");
        check(!c.isInterface(), "ResourceBundle is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ResourceBundle is abstract");
        c.getMethod("getString", String.class);
        c.getMethod("getObject", String.class);
        c.getMethod("getStringArray", String.class);
        c.getMethod("containsKey", String.class);
        c.getMethod("keySet");
        c.getDeclaredMethod("handleGetObject", String.class);
        c.getMethod("getKeys");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ResourceBundle b = new SimpleBundle();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(7).equals(b.getObject("n")), "getObject");
        String[] arr = b.getStringArray("arr");
        check(arr.length == 2 && "a".equals(arr[0]) && "b".equals(arr[1]), "getStringArray");
        check(b.containsKey("k"), "containsKey true");
        check(!b.containsKey("missing"), "containsKey false");

        Set<String> keys = b.keySet();
        check(keys.contains("k") && keys.contains("n") && keys.contains("arr"),
              "keySet contains all keys");

        assertThrows(MissingResourceException.class,
                () -> b.getString("missing"), "getString missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getObject("missing"), "getObject missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getStringArray("missing"), "getStringArray missing throws MissingResourceException");
        assertThrows(ClassCastException.class,
                () -> b.getStringArray("n"), "getStringArray non-array throws ClassCastException");

        check(b.getLocale() == null, "getLocale null for custom bundle (not set)");
        check(b.getBaseBundleName() == null, "getBaseBundleName null for directly-instantiated bundle");

        assertThrows(NullPointerException.class,
                () -> ResourceBundle.getBundle((String) null), "getBundle((String)null) NPE");
        assertThrows(MissingResourceException.class,
                () -> ResourceBundle.getBundle("com.example.DoesNotExistBundle"),
                "getBundle(nonexistent) MissingResourceException");
        ResourceBundle.clearCache();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
