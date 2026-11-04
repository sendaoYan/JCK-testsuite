/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDDescription conformance: class hierarchy and members.
 * @run main XMLDTDDescriptionTest
 */
public class XMLDTDDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDDescription", false, XMLDTDDescriptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl"), "superclass com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarType"), "method getGrammarType");
        check(mn.contains("getRootName"), "method getRootName");
        check(mn.contains("setRootName"), "method setRootName");
        check(mn.contains("setPossibleRoots"), "method setPossibleRoots");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
