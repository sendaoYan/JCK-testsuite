/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.tools.SAXEventSerializer.AttributeValueHolder conformance: class hierarchy and members.
 * @run main AttributeValueHolderTest
 */

public class AttributeValueHolderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.tools.SAXEventSerializer$AttributeValueHolder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.tools.SAXEventSerializer$AttributeValueHolder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.lang.Comparable"), "has interface java.lang.Comparable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("compareTo"), "has method compareTo");
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("hashCode"), "has method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
