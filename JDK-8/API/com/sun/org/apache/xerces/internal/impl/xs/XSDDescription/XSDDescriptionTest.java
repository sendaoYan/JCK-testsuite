/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSDDescription conformance: class hierarchy and members.
 * @run main XSDDescriptionTest
 */
public class XSDDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSDDescription", false, XSDDescriptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl"), "superclass com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarType"), "method getGrammarType");
        check(mn.contains("getContextType"), "method getContextType");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("getLocationHints"), "method getLocationHints");
        check(mn.contains("getTriggeringComponent"), "method getTriggeringComponent");
        check(mn.contains("getEnclosingElementName"), "method getEnclosingElementName");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("fromInstance"), "method fromInstance");
        check(mn.contains("isExternal"), "method isExternal");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("setContextType"), "method setContextType");
        check(mn.contains("setTargetNamespace"), "method setTargetNamespace");
        check(mn.contains("setLocationHints"), "method setLocationHints");
        check(mn.contains("setTriggeringComponent"), "method setTriggeringComponent");
        check(mn.contains("setEnclosingElementName"), "method setEnclosingElementName");
        check(mn.contains("setAttributes"), "method setAttributes");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("makeClone"), "method makeClone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
