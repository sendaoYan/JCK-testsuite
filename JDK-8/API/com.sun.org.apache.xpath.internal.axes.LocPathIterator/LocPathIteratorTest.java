/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.LocPathIterator conformance: reflection.
 * @run main LocPathIteratorTest
 */

public class LocPathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.LocPathIterator", false, LocPathIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LocPathIterator"), "simple name LocPathIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest"), "superclass com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "setEnvironment"), "method setEnvironment");
        check(has(c, "getDTM"), "method getDTM");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "execute"), "method execute");
        check(has(c, "executeCharsToContentHandler"), "method executeCharsToContentHandler");
        check(has(c, "asIterator"), "method asIterator");
        check(has(c, "isNodesetExpr"), "method isNodesetExpr");
        check(has(c, "asNode"), "method asNode");
        check(has(c, "bool"), "method bool");
        check(has(c, "setIsTopLevel"), "method setIsTopLevel");
        check(has(c, "getIsTopLevel"), "method getIsTopLevel");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "getCurrentPos"), "method getCurrentPos");
        check(has(c, "setShouldCacheNodes"), "method setShouldCacheNodes");
        check(has(c, "isMutable"), "method isMutable");
        check(has(c, "setCurrentPos"), "method setCurrentPos");
        check(has(c, "incrementCurrentPos"), "method incrementCurrentPos");
        check(has(c, "size"), "method size");
        check(has(c, "item"), "method item");
        check(has(c, "setItem"), "method setItem");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "isFresh"), "method isFresh");
        check(has(c, "previousNode"), "method previousNode");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "getFilter"), "method getFilter");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "getExpandEntityReferences"), "method getExpandEntityReferences");
        check(has(c, "allowDetachToRelease"), "method allowDetachToRelease");
        check(has(c, "detach"), "method detach");
        check(has(c, "reset"), "method reset");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "runTo"), "method runTo");
        check(has(c, "getFoundLast"), "method getFoundLast");
        check(has(c, "getXPathContext"), "method getXPathContext");
        check(has(c, "getContext"), "method getContext");
        check(has(c, "getCurrentContextNode"), "method getCurrentContextNode");
        check(has(c, "setCurrentContextNode"), "method setCurrentContextNode");
        check(has(c, "getPrefixResolver"), "method getPrefixResolver");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "getLastPos"), "method getLastPos");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
