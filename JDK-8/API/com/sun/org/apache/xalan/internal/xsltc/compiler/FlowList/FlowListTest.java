/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList conformance: reflection.
 * @run main FlowListTest
 */

public class FlowListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList", false, FlowListTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FlowList"), "simple name FlowList");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "add"), "method add");
        check(has(c, "append"), "method append");
        check(has(c, "backPatch"), "method backPatch");
        check(has(c, "copyAndRedirect"), "method copyAndRedirect");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
