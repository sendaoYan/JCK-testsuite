/*
 * @test
 * @summary java.util.concurrent.CopyOnWriteArrayList API conformance: add/get/set/remove,
 *          size, indexOf/contains.
 * @run main CopyOnWriteArrayListTest
 */

import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testList() {
        CopyOnWriteArrayList<String> l = new CopyOnWriteArrayList<>();
        check(l.add("a") && l.add("b"), "add");
        check("a".equals(l.get(0)) && "b".equals(l.get(1)), "get");
        check("a".equals(l.set(0, "A")) && "A".equals(l.get(0)), "set returns old");
        check(l.size() == 2, "size");
        check(l.indexOf("b") == 1 && !l.contains("a") && l.contains("A"), "indexOf/contains");
        check(l.remove("b") && l.size() == 1 && "A".equals(l.get(0)), "remove");
    }

    public static void main(String[] args) {
        testList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
