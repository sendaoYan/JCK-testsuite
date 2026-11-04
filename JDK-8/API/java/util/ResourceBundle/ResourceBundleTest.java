/*
 * @test
 * @summary java.util.ResourceBundle API conformance: getString/getObject/containsKey/keySet via subclass.
 * @run main ResourceBundleTest
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class SimpleBundle extends ResourceBundle {
        private final Map<String, Object> map = new HashMap<String, Object>();
        SimpleBundle() {
            map.put("k", "v");
            map.put("n", Integer.valueOf(7));
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

    public static void main(String[] args) {
        testResourceBundle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
