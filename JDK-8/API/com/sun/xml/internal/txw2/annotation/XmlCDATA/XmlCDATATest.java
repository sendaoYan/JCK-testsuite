/*
 * @test
 * @summary com.sun.xml.internal.txw2.annotation.XmlCDATA conformance: annotation interface hierarchy.
 * @run main XmlCDATATest
 */

public class XmlCDATATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.txw2.annotation.XmlCDATA", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.txw2.annotation.XmlCDATA"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.isAnnotation(), "is annotation");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.lang.annotation.Annotation"), "has interface java.lang.annotation.Annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
