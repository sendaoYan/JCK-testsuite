/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource conformance: interface hierarchy and members.
 * @run main IncrementalSAXSourceTest
 */
public class IncrementalSAXSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource", false, IncrementalSAXSourceTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("setLexicalHandler"), "method setLexicalHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("deliverMoreNodes"), "method deliverMoreNodes");
        check(mn.contains("startParse"), "method startParse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
