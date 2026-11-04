/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl conformance: class hierarchy and members.
 * @run main NamespaceContextImplTest
 */

public class NamespaceContextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.NamespaceContext2"), "has interface com.sun.xml.internal.bind.v2.runtime.NamespaceContext2");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("count"), "has method count");
        check(mn.contains("declareNamespace"), "has method declareNamespace");
        check(mn.contains("declareNsUri"), "has method declareNsUri");
        check(mn.contains("force"), "has method force");
        check(mn.contains("getCurrent"), "has method getCurrent");
        check(mn.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getPrefixIndex"), "has method getPrefixIndex");
        check(mn.contains("getPrefixMapper"), "has method getPrefixMapper");
        check(mn.contains("getPrefixes"), "has method getPrefixes");
        check(mn.contains("makeUniquePrefix"), "has method makeUniquePrefix");
        check(mn.contains("put"), "has method put");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setPrefixMapper"), "has method setPrefixMapper");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
