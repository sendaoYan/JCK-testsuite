/*
 * @test
 * @summary sun.reflect.FieldAccessor conformance: interface and members.
 * @run main FieldAccessorTest
 */

public class FieldAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.FieldAccessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.FieldAccessor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
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
