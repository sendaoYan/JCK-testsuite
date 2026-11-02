/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern conformance: reflection.
 * @run main LocationPathPatternTest
 */

public class LocationPathPatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern", false, LocationPathPatternTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LocationPathPattern"), "simple name LocationPathPattern");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "typeCheck"), "method typeCheck");
        check(has(c, "translate"), "method translate");
        check(has(c, "setTemplate"), "method setTemplate");
        check(has(c, "getTemplate"), "method getTemplate");
        check(has(c, "getPriority"), "method getPriority");
        check(has(c, "getDefaultPriority"), "method getDefaultPriority");
        check(has(c, "noSmallerThan"), "method noSmallerThan");
        check(has(c, "getKernelPattern"), "method getKernelPattern");
        check(has(c, "reduceKernelPattern"), "method reduceKernelPattern");
        check(has(c, "isWildcard"), "method isWildcard");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
