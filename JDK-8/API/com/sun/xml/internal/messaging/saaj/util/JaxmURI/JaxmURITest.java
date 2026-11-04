/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.JaxmURI conformance: class hierarchy and members.
 * @run main JaxmURITest
 */

public class JaxmURITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.JaxmURI", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.JaxmURI"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("appendPath"), "has method appendPath");
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("getFragment"), "has method getFragment");
        check(mn.contains("getHost"), "has method getHost");
        check(mn.contains("getPath"), "has method getPath");
        check(mn.contains("getPort"), "has method getPort");
        check(mn.contains("getQueryString"), "has method getQueryString");
        check(mn.contains("getScheme"), "has method getScheme");
        check(mn.contains("getSchemeSpecificPart"), "has method getSchemeSpecificPart");
        check(mn.contains("getUserinfo"), "has method getUserinfo");
        check(mn.contains("hashCode"), "has method hashCode");
        check(mn.contains("initialize"), "has method initialize");
        check(mn.contains("initializeAuthority"), "has method initializeAuthority");
        check(mn.contains("initializePath"), "has method initializePath");
        check(mn.contains("initializeScheme"), "has method initializeScheme");
        check(mn.contains("isAlpha"), "has method isAlpha");
        check(mn.contains("isAlphanum"), "has method isAlphanum");
        check(mn.contains("isConformantSchemeName"), "has method isConformantSchemeName");
        check(mn.contains("isDigit"), "has method isDigit");
        check(mn.contains("isGenericURI"), "has method isGenericURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
