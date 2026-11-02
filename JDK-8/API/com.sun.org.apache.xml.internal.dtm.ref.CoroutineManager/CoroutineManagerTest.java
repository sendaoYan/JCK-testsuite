/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.CoroutineManager conformance: class hierarchy and members.
 * @run main CoroutineManagerTest
 */
public class CoroutineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.CoroutineManager", false, CoroutineManagerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("co_joinCoroutineSet"), "method co_joinCoroutineSet");
        check(mn.contains("co_entry_pause"), "method co_entry_pause");
        check(mn.contains("co_resume"), "method co_resume");
        check(mn.contains("co_exit"), "method co_exit");
        check(mn.contains("co_exit_to"), "method co_exit_to");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
