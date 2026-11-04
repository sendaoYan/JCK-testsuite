/*
 * @test
 * @summary javax.rmi.CORBA.Util conformance: concrete class reflection.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Util");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "copyObject"), "declares copyObject");
        check(hasMethod(c, "copyObjects"), "declares copyObjects");
        check(hasMethod(c, "createValueHandler"), "declares createValueHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
