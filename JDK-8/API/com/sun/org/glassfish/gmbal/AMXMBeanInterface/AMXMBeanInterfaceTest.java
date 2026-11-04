/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.AMXMBeanInterface conformance: reflection.
 * @run main AMXMBeanInterfaceTest
 */

public class AMXMBeanInterfaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.AMXMBeanInterface", false, AMXMBeanInterfaceTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AMXMBeanInterface"), "simple name AMXMBeanInterface");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getMeta"), "method getMeta");
        check(has(c, "getName"), "method getName");
        check(has(c, "getParent"), "method getParent");
        check(has(c, "getChildren"), "method getChildren");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
