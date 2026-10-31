/*
 * @test
 * @summary java.util.IllegalFormatException API conformance: common supertype of format exceptions.
 * @run main IllegalFormatExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

public class IllegalFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatException() {
        IllegalFormatException a = new MissingFormatArgumentException("s");
        IllegalFormatException b = new UnknownFormatConversionException("y");
        check(a instanceof IllegalArgumentException, "extends IllegalArgumentException");
        check(b instanceof IllegalArgumentException, "all subtypes share the supertype");
    }

    public static void main(String[] args) {
        testIllegalFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
