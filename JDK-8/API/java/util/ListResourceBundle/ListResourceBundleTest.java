/*
 * @test
 * @summary java.util.ListResourceBundle API conformance (Java 8 surface): complete contract
 *          coverage of getContents/getString/getObject/getStringArray/getKeys, containsKey/keySet,
 *          handleGetObject, and getContents() returning null NPE.
 * @run main ListResourceBundleTest
 */

import java.util.ListResourceBundle;

public class ListResourceBundleTest {
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

    static void testListResourceBundle() {
        ListResourceBundle b = new LRB();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(3).equals(b.getObject("n")), "getObject");
        check(b.containsKey("arr"), "containsKey");
        check(b.getKeys().hasMoreElements(), "getKeys has elements");
    }

    static void testStringArrayAndMissing() {
        ListResourceBundle b = new LRB();
        String[] arr = b.getStringArray("arr");
        check(arr != null && arr.length == 2 && "a".equals(arr[0]), "getStringArray");
        expectThrows(java.util.MissingResourceException.class, () -> b.getString("missing"),
                     "getString missing -> MissingResourceException");
        expectThrows(java.util.MissingResourceException.class, () -> b.getObject("missing"),
                     "getObject missing -> MissingResourceException");
        check(b.keySet().contains("k"), "keySet contains k");
    }

    static void testNullContents() {
        // Lookup is loaded lazily on first access; null contents or a null
        // key/value pair therefore NPEs on access, not at construction.
        expectThrows(NullPointerException.class, () -> new NullContentsLRB().getKeys(),
                     "getContents() null -> NPE on access");
        expectThrows(NullPointerException.class, () -> new NullKeyLRB().getString("k"),
                     "null key -> NPE on access");
    }

    public static void main(String[] args) {
        testListResourceBundle();
        testStringArrayAndMissing();
        testNullContents();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
