/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord conformance: reflection.
 * @run main NodeSortRecordTest
 */

public class NodeSortRecordTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord", false, NodeSortRecordTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeSortRecord"), "simple name NodeSortRecord");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "initialize"), "method initialize");
        check(has(c, "getNode"), "method getNode");
        check(has(c, "compareDocOrder"), "method compareDocOrder");
        check(has(c, "compareTo"), "method compareTo");
        check(has(c, "getCollator"), "method getCollator");
        check(has(c, "extractValueFromDOM"), "method extractValueFromDOM");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
