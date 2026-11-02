/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.Repository conformance: reflection.
 * @run main RepositoryTest
 */

public class RepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.Repository", false, RepositoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Repository"), "simple name Repository");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getRepository"), "method getRepository");
        check(has(c, "setRepository"), "method setRepository");
        check(has(c, "lookupClass"), "method lookupClass");
        check(has(c, "clearCache"), "method clearCache");
        check(has(c, "addClass"), "method addClass");
        check(has(c, "removeClass"), "method removeClass");
        check(has(c, "getSuperClasses"), "method getSuperClasses");
        check(has(c, "getInterfaces"), "method getInterfaces");
        check(has(c, "instanceOf"), "method instanceOf");
        check(has(c, "implementationOf"), "method implementationOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
