/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.URI conformance: class hierarchy and members.
 * @run main URITest
 */
public class URITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.URI", false, URITest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getScheme"), "method getScheme");
        check(mn.contains("getSchemeSpecificPart"), "method getSchemeSpecificPart");
        check(mn.contains("getUserinfo"), "method getUserinfo");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPath"), "method getPath");
        check(mn.contains("getQueryString"), "method getQueryString");
        check(mn.contains("getFragment"), "method getFragment");
        check(mn.contains("setScheme"), "method setScheme");
        check(mn.contains("setUserinfo"), "method setUserinfo");
        check(mn.contains("setHost"), "method setHost");
        check(mn.contains("setPort"), "method setPort");
        check(mn.contains("setPath"), "method setPath");
        check(mn.contains("appendPath"), "method appendPath");
        check(mn.contains("setQueryString"), "method setQueryString");
        check(mn.contains("setFragment"), "method setFragment");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("isGenericURI"), "method isGenericURI");
        check(mn.contains("isConformantSchemeName"), "method isConformantSchemeName");
        check(mn.contains("isWellFormedAddress"), "method isWellFormedAddress");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
