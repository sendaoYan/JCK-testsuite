/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMap4Types conformance: class hierarchy and members.
 * @run main XSNamedMap4TypesTest
 */
public class XSNamedMap4TypesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMap4Types", false, XSNamedMap4TypesTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl"), "superclass com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("itemByName"), "method itemByName");
        check(mn.contains("item"), "method item");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
