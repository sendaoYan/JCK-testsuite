/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.NodeSetDTM conformance: reflection.
 * @run main NodeSetDTMTest
 */

public class NodeSetDTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.NodeSetDTM", false, NodeSetDTMTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeSetDTM"), "simple name NodeSetDTM");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.utils.NodeVector"), "superclass com.sun.org.apache.xml.internal.utils.NodeVector");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "setEnvironment"), "method setEnvironment");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "clone"), "method clone");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "reset"), "method reset");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "getFilter"), "method getFilter");
        check(has(c, "getExpandEntityReferences"), "method getExpandEntityReferences");
        check(has(c, "getDTM"), "method getDTM");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "previousNode"), "method previousNode");
        check(has(c, "detach"), "method detach");
        check(has(c, "allowDetachToRelease"), "method allowDetachToRelease");
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
        check(has(c, "size"), "method size");
        check(has(c, "addElement"), "method addElement");
        check(has(c, "insertElementAt"), "method insertElementAt");
        check(has(c, "appendNodes"), "method appendNodes");
        check(has(c, "removeAllElements"), "method removeAllElements");
        check(has(c, "removeElement"), "method removeElement");
        check(has(c, "removeElementAt"), "method removeElementAt");
        check(has(c, "setElementAt"), "method setElementAt");
        check(has(c, "setItem"), "method setItem");
        check(has(c, "elementAt"), "method elementAt");
        check(has(c, "contains"), "method contains");
        check(has(c, "indexOf"), "method indexOf");
        check(has(c, "getCurrentPos"), "method getCurrentPos");
        check(has(c, "setCurrentPos"), "method setCurrentPos");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "getShouldCacheNodes"), "method getShouldCacheNodes");
        check(has(c, "setShouldCacheNodes"), "method setShouldCacheNodes");
        check(has(c, "isMutable"), "method isMutable");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "setLast"), "method setLast");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "getAxis"), "method getAxis");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
