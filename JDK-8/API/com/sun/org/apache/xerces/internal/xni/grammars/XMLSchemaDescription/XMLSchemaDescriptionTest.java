/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription conformance: interface hierarchy and members.
 * @run main XMLSchemaDescriptionTest
 */
public class XMLSchemaDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription", false, XMLSchemaDescriptionTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContextType"), "method getContextType");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("getLocationHints"), "method getLocationHints");
        check(mn.contains("getTriggeringComponent"), "method getTriggeringComponent");
        check(mn.contains("getEnclosingElementName"), "method getEnclosingElementName");
        check(mn.contains("getAttributes"), "method getAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
