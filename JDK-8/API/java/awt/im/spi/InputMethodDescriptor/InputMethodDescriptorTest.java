/*
 * @test
 * @summary java.awt.im.spi.InputMethodDescriptor conformance: interface reflection.
 * @run main InputMethodDescriptorTest
 */

public class InputMethodDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.im.spi.InputMethodDescriptor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "hasDynamicLocaleList"), "method hasDynamicLocaleList");
        check(hasMethod(c, "getInputMethodDisplayName"), "method getInputMethodDisplayName");
        check(hasMethod(c, "getInputMethodIcon"), "method getInputMethodIcon");
        check(hasMethod(c, "createInputMethod"), "method createInputMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
