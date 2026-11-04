/*
 * @test
 * @summary sun.util.locale.provider.HostLocaleProviderAdapterImpl conformance: class and members.
 * @run main HostLocaleProviderAdapterImplTest
 */

public class HostLocaleProviderAdapterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.HostLocaleProviderAdapterImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.HostLocaleProviderAdapterImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
