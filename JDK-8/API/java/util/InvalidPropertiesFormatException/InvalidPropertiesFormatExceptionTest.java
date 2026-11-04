/*
 * @test
 * @summary java.util.InvalidPropertiesFormatException API conformance: constructors and inheritance.
 * @run main InvalidPropertiesFormatExceptionTest
 */

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

public class InvalidPropertiesFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidPropertiesFormatException() {
        InvalidPropertiesFormatException e = new InvalidPropertiesFormatException("m");
        check(e instanceof IOException, "extends IOException");
        check("m".equals(e.getMessage()), "message from (String) ctor");

        RuntimeException cause = new RuntimeException();
        InvalidPropertiesFormatException e2 = new InvalidPropertiesFormatException(cause);
        check(e2.getCause() == cause, "(Throwable) ctor sets cause");
    }


    // ===================== 契约深化：两种构造器语义/cause 无包装 =====================
    static void testIpfContracts() {
        check(InvalidPropertiesFormatException.class.getSuperclass() == java.io.IOException.class,
              "extends IOException (checked)");
        check(InvalidPropertiesFormatException.class.getName().startsWith("java.util."),
              "lives in java.util");
        RuntimeException cause = new RuntimeException("bad-xml");
        InvalidPropertiesFormatException e = new InvalidPropertiesFormatException(cause);
        check(e.getCause() == cause, "(Throwable) ctor stores cause verbatim");
        check(e.getMessage() == null || String.valueOf(e.getMessage()).contains("bad-xml"),
              "(Throwable) ctor message null or derived from cause (impl-dependent)");
        check(new InvalidPropertiesFormatException("m").getCause() == null,
              "(String) ctor leaves cause null");
    }

    public static void main(String[] args) {
        testInvalidPropertiesFormatException();
        testIpfContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
