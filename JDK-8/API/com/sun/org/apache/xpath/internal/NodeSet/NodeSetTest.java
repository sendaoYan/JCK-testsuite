/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.NodeSet conformance: reflection.
 * @run main NodeSetTest
 */

public class NodeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.NodeSet", false, NodeSetTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeSet"), "simple name NodeSet");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.w3c.dom.NodeList"), "interface org.w3c.dom.NodeList");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.w3c.dom.traversal.NodeIterator"), "interface org.w3c.dom.traversal.NodeIterator");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.ContextNodeList"), "interface com.sun.org.apache.xpath.internal.axes.ContextNodeList");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "reset"), "method reset");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "getFilter"), "method getFilter");
        check(has(c, "getExpandEntityReferences"), "method getExpandEntityReferences");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "previousNode"), "method previousNode");
        check(has(c, "detach"), "method detach");
        check(has(c, "isFresh"), "method isFresh");
        check(has(c, "runTo"), "method runTo");
        check(has(c, "item"), "method item");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "addNode"), "method addNode");
        check(has(c, "insertNode"), "method insertNode");
        check(has(c, "removeNode"), "method removeNode");
        check(has(c, "addNodes"), "method addNodes");
        check(has(c, "addNodesInDocOrder"), "method addNodesInDocOrder");
        check(has(c, "addNodeInDocOrder"), "method addNodeInDocOrder");
        check(has(c, "getCurrentPos"), "method getCurrentPos");
        check(has(c, "setCurrentPos"), "method setCurrentPos");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "getShouldCacheNodes"), "method getShouldCacheNodes");
        check(has(c, "setShouldCacheNodes"), "method setShouldCacheNodes");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "setLast"), "method setLast");
        check(has(c, "clone"), "method clone");
        check(has(c, "size"), "method size");
        check(has(c, "addElement"), "method addElement");
        check(has(c, "push"), "method push");
        check(has(c, "pop"), "method pop");
        check(has(c, "popAndTop"), "method popAndTop");
        check(has(c, "popQuick"), "method popQuick");
        check(has(c, "peepOrNull"), "method peepOrNull");
        check(has(c, "pushPair"), "method pushPair");
        check(has(c, "popPair"), "method popPair");
        check(has(c, "setTail"), "method setTail");
        check(has(c, "setTailSub1"), "method setTailSub1");
        check(has(c, "peepTail"), "method peepTail");
        check(has(c, "peepTailSub1"), "method peepTailSub1");
        check(has(c, "insertElementAt"), "method insertElementAt");
        check(has(c, "appendNodes"), "method appendNodes");
        check(has(c, "removeAllElements"), "method removeAllElements");
        check(has(c, "removeElement"), "method removeElement");
        check(has(c, "removeElementAt"), "method removeElementAt");
        check(has(c, "setElementAt"), "method setElementAt");
        check(has(c, "elementAt"), "method elementAt");
        check(has(c, "contains"), "method contains");
        check(has(c, "indexOf"), "method indexOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
