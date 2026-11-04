/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.ObjectStack conformance: class hierarchy and members.
 * @run main ObjectStackTest
 */
public class ObjectStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.ObjectStack", false, ObjectStackTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.utils.ObjectVector"), "superclass com.sun.org.apache.xml.internal.utils.ObjectVector");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("push"), "method push");
        check(mn.contains("pop"), "method pop");
        check(mn.contains("quickPop"), "method quickPop");
        check(mn.contains("peek"), "method peek");
        check(mn.contains("setTop"), "method setTop");
        check(mn.contains("empty"), "method empty");
        check(mn.contains("search"), "method search");
        check(mn.contains("clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
