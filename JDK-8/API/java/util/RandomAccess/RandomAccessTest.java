/*
 * @test
 * @summary java.util.RandomAccess marker interface conformance: ArrayList implements it,
 *          LinkedList does not.
 * @run main RandomAccessTest
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.RandomAccess;

public class RandomAccessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRandomAccess() {
        check(new ArrayList<>() instanceof RandomAccess, "ArrayList is RandomAccess");
        check(!(new LinkedList<>() instanceof RandomAccess), "LinkedList is not RandomAccess");
    }

    public static void main(String[] args) {
        testRandomAccess();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
