/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.FullDVFactory conformance: class hierarchy and members.
 * @run main FullDVFactoryTest
 */
public class FullDVFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.FullDVFactory", false, FullDVFactoryTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.xs.BaseDVFactory"), "superclass com.sun.org.apache.xerces.internal.impl.dv.xs.BaseDVFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBuiltInType"), "method getBuiltInType");
        check(mn.contains("getBuiltInTypes"), "method getBuiltInTypes");
        check(mn.contains("createBuiltInTypes"), "method createBuiltInTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
