/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.patterns.NodeTest conformance: reflection.
 * @run main NodeTestTest
 */

public class NodeTestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.patterns.NodeTest", false, NodeTestTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeTest"), "simple name NodeTest");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "setWhatToShow"), "method setWhatToShow");
        check(has(c, "getNamespace"), "method getNamespace");
        check(has(c, "setNamespace"), "method setNamespace");
        check(has(c, "getLocalName"), "method getLocalName");
        check(has(c, "setLocalName"), "method setLocalName");
        check(has(c, "deepEquals"), "method deepEquals");
        check(has(c, "initNodeTest"), "method initNodeTest");
        check(has(c, "getStaticScore"), "method getStaticScore");
        check(has(c, "setStaticScore"), "method setStaticScore");
        check(has(c, "getDefaultScore"), "method getDefaultScore");
        check(has(c, "getNodeTypeTest"), "method getNodeTypeTest");
        check(has(c, "debugWhatToShow"), "method debugWhatToShow");
        check(has(c, "execute"), "method execute");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "callVisitors"), "method callVisitors");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
