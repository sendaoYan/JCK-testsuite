/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.utils.BoolStack conformance: class hierarchy and members.
 * @run main BoolStackTest
 */
public class BoolStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.utils.BoolStack", false, BoolStackTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("size"), "method size");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("push"), "method push");
        check(mn.contains("pop"), "method pop");
        check(mn.contains("popAndTop"), "method popAndTop");
        check(mn.contains("setTop"), "method setTop");
        check(mn.contains("peek"), "method peek");
        check(mn.contains("peekOrFalse"), "method peekOrFalse");
        check(mn.contains("peekOrTrue"), "method peekOrTrue");
        check(mn.contains("isEmpty"), "method isEmpty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
