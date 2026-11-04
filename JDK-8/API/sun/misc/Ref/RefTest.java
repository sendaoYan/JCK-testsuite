/*
 * @test
 * @summary sun.misc.Ref conformance: abstract class and members.
 * @run main RefTest
 */

public class RefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Ref", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Ref"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("check"), "declares method check");
        check(mnames.contains("flush"), "declares method flush");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("reconstitute"), "declares method reconstitute");
        check(mnames.contains("setThing"), "declares method setThing");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
