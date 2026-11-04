/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.AnyNodeCounter conformance: reflection.
 * @run main AnyNodeCounterTest
 */

public class AnyNodeCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.AnyNodeCounter", false, AnyNodeCounterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AnyNodeCounter"), "simple name AnyNodeCounter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter"), "superclass com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "getCounter"), "method getCounter");
        check(has(c, "getDefaultNodeCounter"), "method getDefaultNodeCounter");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
