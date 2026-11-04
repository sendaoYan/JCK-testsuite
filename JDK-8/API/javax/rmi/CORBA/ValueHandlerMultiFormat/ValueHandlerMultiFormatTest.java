/*
 * @test
 * @summary javax.rmi.CORBA.ValueHandlerMultiFormat conformance: interface reflection.
 * @run main ValueHandlerMultiFormatTest
 */

public class ValueHandlerMultiFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.ValueHandlerMultiFormat");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.rmi.CORBA.ValueHandler").isAssignableFrom(c), "implements/extends javax.rmi.CORBA.ValueHandler");
        check(hasMethod(c, "getMaximumStreamFormatVersion"), "declares getMaximumStreamFormatVersion");
        check(hasMethod(c, "writeValue"), "declares writeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
