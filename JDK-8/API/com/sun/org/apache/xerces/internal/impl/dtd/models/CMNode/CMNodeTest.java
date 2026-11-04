/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode conformance: abstract class hierarchy and members.
 * @run main CMNodeTest
 */
public class CMNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode", false, CMNodeTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isNullable"), "method isNullable");
        check(mn.contains("type"), "method type");
        check(mn.contains("firstPos"), "method firstPos");
        check(mn.contains("lastPos"), "method lastPos");
        check(mn.contains("setFollowPos"), "method setFollowPos");
        check(mn.contains("setMaxStates"), "method setMaxStates");
        check(mn.contains("setUserData"), "method setUserData");
        check(mn.contains("getUserData"), "method getUserData");
        check(mn.contains("calcFirstPos"), "method calcFirstPos");
        check(mn.contains("calcLastPos"), "method calcLastPos");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
