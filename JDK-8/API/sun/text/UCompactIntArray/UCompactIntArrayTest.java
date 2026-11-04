/*
 * @test
 * @summary sun.text.UCompactIntArray conformance: class and members.
 * @run main UCompactIntArrayTest
 */

public class UCompactIntArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.UCompactIntArray", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.UCompactIntArray"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("compact"), "declares method compact");
        check(mnames.contains("elementAt"), "declares method elementAt");
        check(mnames.contains("expand"), "declares method expand");
        check(mnames.contains("getKSize"), "declares method getKSize");
        check(mnames.contains("initPlane"), "declares method initPlane");
        check(mnames.contains("setElementAt"), "declares method setElementAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
