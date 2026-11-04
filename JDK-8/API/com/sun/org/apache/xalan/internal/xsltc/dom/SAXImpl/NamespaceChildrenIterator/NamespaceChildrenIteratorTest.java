/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl.NamespaceChildrenIterator conformance: reflection.
 * @run main NamespaceChildrenIteratorTest
 */

public class NamespaceChildrenIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl$NamespaceChildrenIterator", false, NamespaceChildrenIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NamespaceChildrenIterator"), "simple name NamespaceChildrenIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators$InternalAxisIteratorBase"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators$InternalAxisIteratorBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "next"), "method next");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
