/*
 * @test
 * @summary sun.net.ResolverProviderConfiguration conformance: internal package reflection.
 * @modules java.base/sun.net:+open
 * @run main ResolverProviderConfigurationTest
 */
public class ResolverProviderConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ResolverProviderConfiguration", false, ResolverProviderConfigurationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.net.spi.InetAddressResolverProvider$Configuration"), "interface java.net.spi.InetAddressResolverProvider$Configuration");
        check(hasMethod(c, "builtinResolver"), "method builtinResolver");
        check(hasMethod(c, "lookupLocalHostName"), "method lookupLocalHostName");
        check(hasField(c, "builtinResolver"), "field builtinResolver");
        check(hasField(c, "localHostNameSupplier"), "field localHostNameSupplier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
