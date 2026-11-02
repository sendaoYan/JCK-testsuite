/*
 * @test
 * @summary sun.reflect.ConstantPool conformance: class and members.
 * @run main ConstantPoolTest
 */

public class ConstantPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.ConstantPool", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.ConstantPool"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getClassAt"), "declares method getClassAt");
        check(mnames.contains("getClassAt0"), "declares method getClassAt0");
        check(mnames.contains("getClassAtIfLoaded"), "declares method getClassAtIfLoaded");
        check(mnames.contains("getClassAtIfLoaded0"), "declares method getClassAtIfLoaded0");
        check(mnames.contains("getDoubleAt"), "declares method getDoubleAt");
        check(mnames.contains("getDoubleAt0"), "declares method getDoubleAt0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
