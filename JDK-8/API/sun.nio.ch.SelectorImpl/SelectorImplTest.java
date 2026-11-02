/*
 * @test
 * @summary sun.nio.ch.SelectorImpl conformance: abstract class and members.
 * @run main SelectorImplTest
 */

public class SelectorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectorImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.SelectorImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelector"), "superclass java.nio.channels.spi.AbstractSelector");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("doSelect"), "declares method doSelect");
        check(mnames.contains("implClose"), "declares method implClose");
        check(mnames.contains("implCloseSelector"), "declares method implCloseSelector");
        check(mnames.contains("implDereg"), "declares method implDereg");
        check(mnames.contains("implRegister"), "declares method implRegister");
        check(mnames.contains("keys"), "declares method keys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
