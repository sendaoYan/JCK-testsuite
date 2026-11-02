/*
 * @test
 * @summary sun.reflect.FieldInfo conformance: class and members.
 * @run main FieldInfoTest
 */

public class FieldInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.FieldInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.FieldInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("isPublic"), "declares method isPublic");
        check(mnames.contains("modifiers"), "declares method modifiers");
        check(mnames.contains("name"), "declares method name");
        check(mnames.contains("signature"), "declares method signature");
        check(mnames.contains("slot"), "declares method slot");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
