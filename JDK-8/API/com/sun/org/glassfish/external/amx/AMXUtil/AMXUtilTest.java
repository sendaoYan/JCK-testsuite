/*
 * @test
 * @summary com.sun.org.glassfish.external.amx.AMXUtil conformance: reflection.
 * @run main AMXUtilTest
 */

public class AMXUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.amx.AMXUtil", false, AMXUtilTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AMXUtil"), "simple name AMXUtil");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "newObjectName"), "method newObjectName");
        check(has(c, "getMBeanServerDelegateObjectName"), "method getMBeanServerDelegateObjectName");
        check(has(c, "prop"), "method prop");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
