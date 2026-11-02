/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.NodeInfo conformance: reflection.
 * @run main NodeInfoTest
 */

public class NodeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.NodeInfo", false, NodeInfoTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeInfo"), "simple name NodeInfo");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "systemId"), "method systemId");
        check(has(c, "publicId"), "method publicId");
        check(has(c, "lineNumber"), "method lineNumber");
        check(has(c, "columnNumber"), "method columnNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
