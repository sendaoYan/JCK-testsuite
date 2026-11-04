/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMTreeWalker conformance: class hierarchy and members.
 * @run main DTMTreeWalkerTest
 */
public class DTMTreeWalkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMTreeWalker", false, DTMTreeWalkerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDTM"), "method setDTM");
        check(mn.contains("getcontentHandler"), "method getcontentHandler");
        check(mn.contains("setcontentHandler"), "method setcontentHandler");
        check(mn.contains("traverse"), "method traverse");
        check(mn.contains("startNode"), "method startNode");
        check(mn.contains("endNode"), "method endNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
