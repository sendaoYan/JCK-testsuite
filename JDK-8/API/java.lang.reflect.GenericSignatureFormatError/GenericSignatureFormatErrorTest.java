/*
 * @test
 * @summary java.lang.reflect.GenericSignatureFormatError conformance: constructors/hierarchy.
 * @run main GenericSignatureFormatErrorTest
 */

import java.lang.reflect.GenericSignatureFormatError;

public class GenericSignatureFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGenericSignatureFormatError() {
        GenericSignatureFormatError e = new GenericSignatureFormatError();
        check(e instanceof ClassFormatError, "is ClassFormatError");
        GenericSignatureFormatError e2 = new GenericSignatureFormatError("bad signature");
        check("bad signature".equals(e2.getMessage()), "message");
    }

    public static void main(String[] args) {
        testGenericSignatureFormatError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
