/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.TypedAncestorIterator conformance: class hierarchy and members.
 * @run main TypedAncestorIteratorTest
 */
public class TypedAncestorIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2$TypedAncestorIterator", false, TypedAncestorIteratorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2$AncestorIterator"), "superclass com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2$AncestorIterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setStartNode"), "method setStartNode");
        check(mn.contains("getNodeByPosition"), "method getNodeByPosition");
        check(mn.contains("getLast"), "method getLast");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
