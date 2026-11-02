/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes conformance: interface hierarchy and members.
 * @run main EncodingAlgorithmAttributesTest
 */

public class EncodingAlgorithmAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("org.xml.sax.Attributes"), "has interface org.xml.sax.Attributes");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAlgorithmData"), "has method getAlgorithmData");
        check(mn.contains("getAlgorithmIndex"), "has method getAlgorithmIndex");
        check(mn.contains("getAlgorithmURI"), "has method getAlgorithmURI");
        check(mn.contains("getAlpababet"), "has method getAlpababet");
        check(mn.contains("getToIndex"), "has method getToIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
