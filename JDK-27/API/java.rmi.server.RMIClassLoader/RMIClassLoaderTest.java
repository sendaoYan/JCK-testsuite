/*
 * @test
 * @summary java.rmi.server.RMIClassLoader conformance: white-box structure + black-box behavior.
 * @run main RMIClassLoaderTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

public class RMIClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = RMIClassLoader.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getDeclaredConstructors().length == 1, "single (private) ctor");
        check(!Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "ctor not public");
    }

    @SuppressWarnings("deprecation")
    static void testBlackBox() throws Exception {
        // bootstrap class loads without a codebase
        check(RMIClassLoader.loadClass("java.lang.String") == String.class, "loadClass(String)");
        check(RMIClassLoader.loadClass((String) null, "java.lang.String") == String.class, "loadClass(null, name)");
        check(RMIClassLoader.loadClass(null, "java.lang.String", ClassLoader.getSystemClassLoader()) == String.class, "loadClass(null, name, loader)");

        // bootstrap class has no codebase annotation
        check(RMIClassLoader.getClassAnnotation(String.class) == null, "getClassAnnotation bootstrap null");

        // security context for the system class loader
        check(RMIClassLoader.getSecurityContext(ClassLoader.getSystemClassLoader()) == null, "getSecurityContext system null");

        // default provider instance is a non-null RMIClassLoaderSpi
        RMIClassLoaderSpi provider = RMIClassLoader.getDefaultProviderInstance();
        check(provider != null, "getDefaultProviderInstance non-null");
        check(provider instanceof RMIClassLoaderSpi, "default provider is RMIClassLoaderSpi");
        check(RMIClassLoader.getDefaultProviderInstance() == provider, "default provider stable singleton");

        // getClassLoader(null) returns a usable class loader
        ClassLoader cl = RMIClassLoader.getClassLoader(null);
        check(cl != null, "getClassLoader(null) non-null");
        check(cl.loadClass("java.lang.String") == String.class, "returned loader loads String");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
