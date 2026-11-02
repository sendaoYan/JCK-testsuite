/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl.Element conformance: class hierarchy and members.
 * @run main ElementTest
 */

public class ElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl$Element", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl$Element"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("count"), "has method count");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("getBase"), "has method getBase");
        check(mn.contains("getInnerPeer"), "has method getInnerPeer");
        check(mn.contains("getNsUri"), "has method getNsUri");
        check(mn.contains("getOuterPeer"), "has method getOuterPeer");
        check(mn.contains("getParent"), "has method getParent");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("isRootElement"), "has method isRootElement");
        check(mn.contains("onPushed"), "has method onPushed");
        check(mn.contains("pop"), "has method pop");
        check(mn.contains("push"), "has method push");
        check(mn.contains("setTagName"), "has method setTagName");
        check(mn.contains("startElement"), "has method startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
