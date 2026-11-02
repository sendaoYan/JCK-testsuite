/*
 * @test
 * @summary sun.corba.Bridge conformance: class and members.
 * @run main BridgeTest
 */

public class BridgeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.corba.Bridge", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.corba.Bridge"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getBoolean"), "declares method getBoolean");
        check(mnames.contains("getByte"), "declares method getByte");
        check(mnames.contains("getChar"), "declares method getChar");
        check(mnames.contains("getDouble"), "declares method getDouble");
        check(mnames.contains("getFloat"), "declares method getFloat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
