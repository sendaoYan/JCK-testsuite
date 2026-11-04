/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xslt.EnvironmentCheck conformance: reflection.
 * @run main EnvironmentCheckTest
 */

public class EnvironmentCheckTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xslt.EnvironmentCheck", false, EnvironmentCheckTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("EnvironmentCheck"), "simple name EnvironmentCheck");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "main"), "method main");
        check(has(c, "checkEnvironment"), "method checkEnvironment");
        check(has(c, "getEnvironmentHash"), "method getEnvironmentHash");
        check(has(c, "appendEnvironmentReport"), "method appendEnvironmentReport");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
