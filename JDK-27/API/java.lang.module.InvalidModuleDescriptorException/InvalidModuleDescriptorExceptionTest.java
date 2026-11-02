/*
 * @test
 * @summary java.lang.module.InvalidModuleDescriptorException conformance: RuntimeException subclass and constructors.
 * @run main InvalidModuleDescriptorExceptionTest
 */

import java.lang.module.InvalidModuleDescriptorException;

public class InvalidModuleDescriptorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(new InvalidModuleDescriptorException() instanceof RuntimeException, "extends RuntimeException");

        InvalidModuleDescriptorException e1 = new InvalidModuleDescriptorException("bad descriptor");
        check("bad descriptor".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        check(new InvalidModuleDescriptorException().getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
