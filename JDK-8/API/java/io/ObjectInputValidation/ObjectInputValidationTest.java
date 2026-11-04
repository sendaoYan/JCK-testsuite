/*
 * @test
 * @summary java.io.ObjectInputValidation API conformance: validateObject.
 * @run main ObjectInputValidationTest
 */

import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;

public class ObjectInputValidationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectInputValidation() throws InvalidObjectException {
        final boolean[] called = new boolean[1];
        ObjectInputValidation v = new ObjectInputValidation() {
            public void validateObject() throws InvalidObjectException {
                called[0] = true;
            }
        };
        v.validateObject();
        check(called[0], "validateObject invokes the implementation");
    }

    public static void main(String[] args) throws InvalidObjectException {
        testObjectInputValidation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
