/*
 * @test
 * @summary java.util.RandomAccess conformance: white-box structure + black-box behavior.
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.RandomAccess");
        check(c.isInterface(), "is an interface");
        check(c.getDeclaredMethods().length == 0, "marker interface, no members");
    }

    static void testBlackBox() {
        check(new ArrayList<>() instanceof RandomAccess, "ArrayList is RandomAccess");
        check(!(new LinkedList<>() instanceof RandomAccess), "LinkedList is not RandomAccess");
        check(RandomAccess.class.isAssignableFrom(ArrayList.class), "assignable from ArrayList");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
