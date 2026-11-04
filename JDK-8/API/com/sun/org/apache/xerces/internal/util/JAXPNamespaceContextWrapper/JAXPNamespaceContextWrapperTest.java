/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.JAXPNamespaceContextWrapper conformance: class hierarchy and members.
 * @run main JAXPNamespaceContextWrapperTest
 */
public class JAXPNamespaceContextWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.JAXPNamespaceContextWrapper", false, JAXPNamespaceContextWrapperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.NamespaceContext"), "has interface com.sun.org.apache.xerces.internal.xni.NamespaceContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setNamespaceContext"), "method setNamespaceContext");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("setSymbolTable"), "method setSymbolTable");
        check(mn.contains("getSymbolTable"), "method getSymbolTable");
        check(mn.contains("setDeclaredPrefixes"), "method setDeclaredPrefixes");
        check(mn.contains("getDeclaredPrefixes"), "method getDeclaredPrefixes");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getAllPrefixes"), "method getAllPrefixes");
        check(mn.contains("pushContext"), "method pushContext");
        check(mn.contains("popContext"), "method popContext");
        check(mn.contains("declarePrefix"), "method declarePrefix");
        check(mn.contains("getDeclaredPrefixCount"), "method getDeclaredPrefixCount");
        check(mn.contains("getDeclaredPrefixAt"), "method getDeclaredPrefixAt");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
