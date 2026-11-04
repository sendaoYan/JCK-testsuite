/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.UnionPathIterator conformance: reflection.
 * @run main UnionPathIteratorTest
 */

public class UnionPathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.UnionPathIterator", false, UnionPathIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("UnionPathIterator"), "simple name UnionPathIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.LocPathIterator"), "superclass com.sun.org.apache.xpath.internal.axes.LocPathIterator");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "addIterator"), "method addIterator");
        check(has(c, "detach"), "method detach");
        check(has(c, "createUnionIterator"), "method createUnionIterator");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "clone"), "method clone");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
