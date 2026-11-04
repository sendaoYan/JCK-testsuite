/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.patterns.NodeTestFilter conformance: reflection.
 * @run main NodeTestFilterTest
 */

public class NodeTestFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.patterns.NodeTestFilter", false, NodeTestFilterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeTestFilter"), "simple name NodeTestFilter");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setNodeTest"), "method setNodeTest");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
