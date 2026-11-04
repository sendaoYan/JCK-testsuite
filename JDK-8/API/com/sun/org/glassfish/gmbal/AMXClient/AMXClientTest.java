/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.AMXClient conformance: reflection.
 * @run main AMXClientTest
 */

public class AMXClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.AMXClient", false, AMXClientTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AMXClient"), "simple name AMXClient");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.gmbal.AMXMBeanInterface"), "interface com.sun.org.glassfish.gmbal.AMXMBeanInterface");
        check(has(c, "equals"), "method equals");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "toString"), "method toString");
        check(has(c, "getName"), "method getName");
        check(has(c, "getMeta"), "method getMeta");
        check(has(c, "getParent"), "method getParent");
        check(has(c, "getChildren"), "method getChildren");
        check(has(c, "getAttribute"), "method getAttribute");
        check(has(c, "setAttribute"), "method setAttribute");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "setAttributes"), "method setAttributes");
        check(has(c, "invoke"), "method invoke");
        check(has(c, "getMBeanInfo"), "method getMBeanInfo");
        check(has(c, "objectName"), "method objectName");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
