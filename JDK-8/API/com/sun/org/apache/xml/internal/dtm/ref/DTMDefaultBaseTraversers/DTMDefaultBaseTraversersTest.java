/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers conformance: abstract class hierarchy and members.
 * @run main DTMDefaultBaseTraversersTest
 */
public class DTMDefaultBaseTraversersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers", false, DTMDefaultBaseTraversersTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAxisTraverser"), "method getAxisTraverser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
