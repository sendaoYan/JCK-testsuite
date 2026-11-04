/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Xerces conformance: class hierarchy and members.
 * @run main IncrementalSAXSource_XercesTest
 */
public class IncrementalSAXSource_XercesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Xerces", false, IncrementalSAXSource_XercesTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource"), "has interface com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createIncrementalSAXSource"), "method createIncrementalSAXSource");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("setLexicalHandler"), "method setLexicalHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("startParse"), "method startParse");
        check(mn.contains("deliverMoreNodes"), "method deliverMoreNodes");
        check(mn.contains("_main"), "method _main");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
