/*
 * @test
 * @summary java.rmi.server.RMIClassLoaderSpi conformance: white-box structure + black-box behavior.
 * @run main RMIClassLoaderSpiTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

public class RMIClassLoaderSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMIClassLoaderSpi");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "public no-arg ctor");
    }

    static void testBlackBox() throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();

        RMIClassLoaderSpi spi = new RMIClassLoaderSpi() {
            public Class<?> loadClass(String codebase, String name, ClassLoader cl) throws ClassNotFoundException {
                return Class.forName(name, false, cl);
            }
            public Class<?> loadProxyClass(String codebase, String[] ifaces, ClassLoader cl) {
                return null;
            }
            public ClassLoader getClassLoader(String codebase) {
                return loader;
            }
            public String getClassAnnotation(Class<?> c) {
                return "annot:" + c.getName();
            }
        };

        check(spi instanceof RMIClassLoaderSpi, "anonymous instanceof RMIClassLoaderSpi");
        check(spi.loadClass(null, "java.lang.String", loader) == String.class, "loadClass delegates");
        check(spi.loadClass(null, "java.lang.Integer", loader) == Integer.class, "loadClass another class");
        check(spi.loadProxyClass(null, new String[]{"java.io.Serializable"}, loader) == null, "loadProxyClass sentinel");
        check(spi.getClassLoader(null) == loader, "getClassLoader returns loader");
        check("annot:java.lang.String".equals(spi.getClassAnnotation(String.class)), "getClassAnnotation format");

        // the default provider returned by RMIClassLoader is an RMIClassLoaderSpi
        check(RMIClassLoader.getDefaultProviderInstance() instanceof RMIClassLoaderSpi, "default provider instanceof RMIClassLoaderSpi");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
