/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.sax.AttributesHolder conformance: class hierarchy and members.
 * @run main AttributesHolderTest
 */

public class AttributesHolderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.sax.AttributesHolder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.sax.AttributesHolder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttribute"), "has method addAttribute");
        check(mn.contains("addAttributeWithAlgorithmData"), "has method addAttributeWithAlgorithmData");
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("convertEncodingAlgorithmDataToString"), "has method convertEncodingAlgorithmDataToString");
        check(mn.contains("getAlgorithmData"), "has method getAlgorithmData");
        check(mn.contains("getAlgorithmIndex"), "has method getAlgorithmIndex");
        check(mn.contains("getAlgorithmURI"), "has method getAlgorithmURI");
        check(mn.contains("getAlpababet"), "has method getAlpababet");
        check(mn.contains("getIndex"), "has method getIndex");
        check(mn.contains("getLength"), "has method getLength");
        check(mn.contains("getLocalName"), "has method getLocalName");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getQName"), "has method getQName");
        check(mn.contains("getQualifiedName"), "has method getQualifiedName");
        check(mn.contains("getToIndex"), "has method getToIndex");
        check(mn.contains("getType"), "has method getType");
        check(mn.contains("getURI"), "has method getURI");
        check(mn.contains("getValue"), "has method getValue");
        check(mn.contains("resize"), "has method resize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
