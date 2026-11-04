/*
 * @test
 * @summary java.lang.module.InvalidModuleDescriptorException conformance: white-box + black-box behavior.
 * @run main InvalidModuleDescriptorExceptionTest
 */

import java.lang.module.InvalidModuleDescriptorException;

public class InvalidModuleDescriptorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.InvalidModuleDescriptorException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "exactly two public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(new InvalidModuleDescriptorException() instanceof RuntimeException,
              "is a RuntimeException");

        InvalidModuleDescriptorException e0 = new InvalidModuleDescriptorException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");

        InvalidModuleDescriptorException e1 =
                new InvalidModuleDescriptorException("bad descriptor");
        check("bad descriptor".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        check(e1.toString().startsWith(
                      "java.lang.module.InvalidModuleDescriptorException: bad descriptor"),
              "toString format");
        e1.addSuppressed(new IllegalStateException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");
        // message-only constructor never carries a cause; initCause is available
        RuntimeException c = new RuntimeException("c");
        e1.initCause(c);
        check(e1.getCause() == c, "initCause sets cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
