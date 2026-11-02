/*
 * @test
 * @summary sun.net.spi.nameservice.NameServiceDescriptor conformance: interface and members.
 * @run main NameServiceDescriptorTest
 */

public class NameServiceDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.spi.nameservice.NameServiceDescriptor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.spi.nameservice.NameServiceDescriptor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createNameService"), "declares method createNameService");
        check(mnames.contains("getProviderName"), "declares method getProviderName");
        check(mnames.contains("getType"), "declares method getType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
