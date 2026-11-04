/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.CDATAImpl conformance: class hierarchy and members.
 * @run main CDATAImplTest
 */

public class CDATAImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.CDATAImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.CDATAImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.org.apache.xerces.internal.dom.CDATASectionImpl".equals(c.getSuperclass().getName()), "extends com.sun.org.apache.xerces.internal.dom.CDATASectionImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.Text"), "has interface javax.xml.soap.Text");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("detachNode"), "has method detachNode");
        check(mn.contains("getParentElement"), "has method getParentElement");
        check(mn.contains("getValue"), "has method getValue");
        check(mn.contains("isComment"), "has method isComment");
        check(mn.contains("recycleNode"), "has method recycleNode");
        check(mn.contains("setParentElement"), "has method setParentElement");
        check(mn.contains("setValue"), "has method setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
