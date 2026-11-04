/*
 * @test
 * @summary java.lang.Comparable API conformance: compareTo sign (negative/zero/positive),
 *          antisymmetry of Integer/String natural ordering, self-comparison == 0, and
 *          consistency with Collections.sort.
 * @run main ComparableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComparableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInteger() {
        Integer a = Integer.valueOf(3), b = Integer.valueOf(7), c = Integer.valueOf(3);
        check(a.compareTo(b) < 0, "3.compareTo(7) < 0");
        check(b.compareTo(a) > 0, "7.compareTo(3) > 0");
        check(a.compareTo(c) == 0, "3.compareTo(3) == 0");
        check(a.compareTo(a) == 0, "self compareTo == 0");
        check(Integer.compare(a, b) == a.compareTo(b), "Integer.compare consistent");
    }

    static void testString() {
        check("apple".compareTo("banana") < 0, "\"apple\".compareTo(\"banana\") < 0");
        check("banana".compareTo("apple") > 0, "\"banana\".compareTo(\"apple\") > 0");
        check("same".compareTo("same") == 0, "equal strings compareTo == 0");
    }

    static void testSort() {
        List<Integer> xs = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        Collections.sort(xs);
        check(xs.equals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9)), "Collections.sort natural order");
    }

    public static void main(String[] args) {
        testInteger();
        testString();
        testSort();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
