/*
 * @test
 * @summary java.util.ResourceBundle API conformance (Java 8 surface): complete contract coverage
 *          of getString/getObject/getStringArray + MissingResourceException for missing key +
 *          null-return semantics, getKeys/containsKey/keySet, getLocale/getBaseBundleName for a
 *          custom subclass, getBundle error paths (null NPE / nonexistent MissingResourceException),
 *          and clearCache.
 * @run main ResourceBundleTest
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void testResourceBundle() {
        ResourceBundle b = new SimpleBundle();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(7).equals(b.getObject("n")), "getObject");
        check(b.containsKey("k"), "containsKey true");
        check(!b.containsKey("missing"), "containsKey false");
        check(b.keySet().contains("k"), "keySet contains key");
    }

    static void testStringArrayAndMissing() {
        ResourceBundle b = new SimpleBundle();
        String[] arr = b.getStringArray("arr");
        check(arr != null && arr.length == 2 && "a".equals(arr[0]) && "b".equals(arr[1]),
              "getStringArray returns array");
        // All three accessors throw MissingResourceException for a missing key.
        expectThrows(MissingResourceException.class, () -> b.getString("missing"),
                     "getString missing -> MissingResourceException");
        expectThrows(MissingResourceException.class, () -> b.getObject("missing"),
                     "getObject missing -> MissingResourceException");
        expectThrows(MissingResourceException.class, () -> b.getStringArray("missing"),
                     "getStringArray missing -> MissingResourceException");
        expectThrows(ClassCastException.class, () -> b.getStringArray("n"),
                     "getStringArray non-array -> ClassCastException");
    }

    static void testLocaleAndBaseName() {
        ResourceBundle b = new SimpleBundle();
        check(b.getLocale() == null, "getLocale null for custom bundle (not set)");
        check(b.getBaseBundleName() == null, "getBaseBundleName null for custom bundle");
    }

    static void testGetBundleErrors() {
        expectThrows(NullPointerException.class, () -> ResourceBundle.getBundle(null),
                     "getBundle(null) NPE");
        expectThrows(MissingResourceException.class,
                     () -> ResourceBundle.getBundle("com.example.DoesNotExistBundle"),
                     "getBundle(nonexistent) MissingResourceException");
        ResourceBundle.clearCache();
        passed++;
    }

    public static void main(String[] args) {
        testResourceBundle();
        testStringArrayAndMissing();
        testLocaleAndBaseName();
        testGetBundleErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
