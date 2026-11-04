/*
 * @test
 * @summary java.beans.XMLDecoder conformance: concrete class reflection.
 * @run main XMLDecoderTest
 */

public class XMLDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.XMLDecoder");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.lang.AutoCloseable").isAssignableFrom(c), "implements/extends java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "createHandler"), "declares createHandler");
        check(hasMethod(c, "getExceptionListener"), "declares getExceptionListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
