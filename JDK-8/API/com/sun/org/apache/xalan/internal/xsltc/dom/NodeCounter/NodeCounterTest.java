/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter conformance: reflection.
 * @run main NodeCounterTest
 */

public class NodeCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter", false, NodeCounterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeCounter"), "simple name NodeCounter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "setValue"), "method setValue");
        check(has(c, "setDefaultFormatting"), "method setDefaultFormatting");
        check(has(c, "getCounter"), "method getCounter");
        check(has(c, "matchesCount"), "method matchesCount");
        check(has(c, "matchesFrom"), "method matchesFrom");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
