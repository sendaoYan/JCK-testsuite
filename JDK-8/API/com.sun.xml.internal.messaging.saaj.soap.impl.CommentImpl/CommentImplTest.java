/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.CommentImpl conformance: class hierarchy and members.
 * @run main CommentImplTest
 */

public class CommentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.CommentImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.CommentImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.org.apache.xerces.internal.dom.CommentImpl".equals(c.getSuperclass().getName()), "extends com.sun.org.apache.xerces.internal.dom.CommentImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("javax.xml.soap.Text"), "has interface javax.xml.soap.Text");
        check(ifs.contains("org.w3c.dom.Comment"), "has interface org.w3c.dom.Comment");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("detachNode"), "has method detachNode");
        check(mn.contains("getParentElement"), "has method getParentElement");
        check(mn.contains("getValue"), "has method getValue");
        check(mn.contains("getWholeText"), "has method getWholeText");
        check(mn.contains("isComment"), "has method isComment");
        check(mn.contains("isElementContentWhitespace"), "has method isElementContentWhitespace");
        check(mn.contains("recycleNode"), "has method recycleNode");
        check(mn.contains("replaceWholeText"), "has method replaceWholeText");
        check(mn.contains("setParentElement"), "has method setParentElement");
        check(mn.contains("setValue"), "has method setValue");
        check(mn.contains("splitText"), "has method splitText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
