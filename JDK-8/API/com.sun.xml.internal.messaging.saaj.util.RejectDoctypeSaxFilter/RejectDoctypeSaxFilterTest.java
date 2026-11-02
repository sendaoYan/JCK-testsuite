/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.RejectDoctypeSaxFilter conformance: class hierarchy and members.
 * @run main RejectDoctypeSaxFilterTest
 */

public class RejectDoctypeSaxFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.RejectDoctypeSaxFilter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.RejectDoctypeSaxFilter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("org.xml.sax.helpers.XMLFilterImpl".equals(c.getSuperclass().getName()), "extends org.xml.sax.helpers.XMLFilterImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("org.xml.sax.XMLReader"), "has interface org.xml.sax.XMLReader");
        check(ifs.contains("org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("endCDATA"), "has method endCDATA");
        check(mn.contains("endDTD"), "has method endDTD");
        check(mn.contains("endEntity"), "has method endEntity");
        check(mn.contains("setProperty"), "has method setProperty");
        check(mn.contains("startCDATA"), "has method startCDATA");
        check(mn.contains("startDTD"), "has method startDTD");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("startEntity"), "has method startEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
