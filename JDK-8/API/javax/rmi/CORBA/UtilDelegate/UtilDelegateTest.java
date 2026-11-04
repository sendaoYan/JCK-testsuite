/*
 * @test
 * @summary javax.rmi.CORBA.UtilDelegate conformance: interface reflection.
 * @run main UtilDelegateTest
 */

public class UtilDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.UtilDelegate");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "copyObject"), "declares copyObject");
        check(hasMethod(c, "copyObjects"), "declares copyObjects");
        check(hasMethod(c, "createValueHandler"), "declares createValueHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
