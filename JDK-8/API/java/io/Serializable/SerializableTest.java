/*
 * @test
 * @summary java.io.Serializable marker interface conformance: ArrayList and String implement
 *          it, a plain Object does not.
 * @run main SerializableTest
 */

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSerializable() {
        check(new ArrayList<>() instanceof Serializable, "ArrayList is Serializable");
        check("x" instanceof Serializable, "String is Serializable");
        check(!(new Object() instanceof Serializable), "plain Object is not Serializable");
    }

    public static void main(String[] args) {
        testSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
