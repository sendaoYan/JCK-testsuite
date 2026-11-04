/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.NodeSequence conformance: reflection.
 * @run main NodeSequenceTest
 */

public class NodeSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.NodeSequence", false, NodeSequenceTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeSequence"), "simple name NodeSequence");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XObject"), "superclass com.sun.org.apache.xpath.internal.objects.XObject");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(has(c, "hasCache"), "method hasCache");
        check(has(c, "setIter"), "method setIter");
        check(has(c, "getContainedIter"), "method getContainedIter");
        check(has(c, "getDTM"), "method getDTM");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "reset"), "method reset");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "getExpandEntityReferences"), "method getExpandEntityReferences");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "previousNode"), "method previousNode");
        check(has(c, "detach"), "method detach");
        check(has(c, "allowDetachToRelease"), "method allowDetachToRelease");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "isFresh"), "method isFresh");
        check(has(c, "setShouldCacheNodes"), "method setShouldCacheNodes");
        check(has(c, "isMutable"), "method isMutable");
        check(has(c, "getCurrentPos"), "method getCurrentPos");
        check(has(c, "runTo"), "method runTo");
        check(has(c, "setCurrentPos"), "method setCurrentPos");
        check(has(c, "item"), "method item");
        check(has(c, "setItem"), "method setItem");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "clone"), "method clone");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "fixupVariables"), "method fixupVariables");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
