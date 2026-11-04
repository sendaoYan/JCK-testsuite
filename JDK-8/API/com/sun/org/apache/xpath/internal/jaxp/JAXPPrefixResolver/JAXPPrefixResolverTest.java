/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.jaxp.JAXPPrefixResolver conformance: reflection.
 * @run main JAXPPrefixResolverTest
 */

public class JAXPPrefixResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.jaxp.JAXPPrefixResolver", false, JAXPPrefixResolverTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("JAXPPrefixResolver"), "simple name JAXPPrefixResolver");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.utils.PrefixResolver"), "interface com.sun.org.apache.xml.internal.utils.PrefixResolver");
        check(has(c, "getNamespaceForPrefix"), "method getNamespaceForPrefix");
        check(has(c, "getBaseIdentifier"), "method getBaseIdentifier");
        check(has(c, "handlesNullPrefixes"), "method handlesNullPrefixes");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
