/*
 * @test
 * @summary java.util.Dictionary API conformance: put/get/size/keys/elements via a Hashtable.
 * @run main DictionaryTest
 */

import java.util.Dictionary;
import java.util.Hashtable;

public class DictionaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDictionary() {
        Dictionary<String, Integer> d = new Hashtable<>();
        d.put("a", 1);
        check(d.size() == 1, "size");
        check(d.get("a") == 1, "get");
        check(d.keys().hasMoreElements(), "keys");
        check(d.elements().hasMoreElements(), "elements");
        check(d.remove("a") == 1, "remove");
        check(d.isEmpty(), "empty after remove");
    }

    public static void main(String[] args) {
        testDictionary();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
