/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText conformance: class hierarchy and members.
 * @run main DefaultTextTest
 */
public class DefaultTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText", false, DefaultTextTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Text"), "has interface org.w3c.dom.Text");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getData"), "method getData");
        check(mn.contains("setData"), "method setData");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("substringData"), "method substringData");
        check(mn.contains("appendData"), "method appendData");
        check(mn.contains("insertData"), "method insertData");
        check(mn.contains("deleteData"), "method deleteData");
        check(mn.contains("replaceData"), "method replaceData");
        check(mn.contains("splitText"), "method splitText");
        check(mn.contains("isElementContentWhitespace"), "method isElementContentWhitespace");
        check(mn.contains("getWholeText"), "method getWholeText");
        check(mn.contains("replaceWholeText"), "method replaceWholeText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
