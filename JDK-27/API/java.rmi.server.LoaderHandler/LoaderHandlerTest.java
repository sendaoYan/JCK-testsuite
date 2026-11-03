/*
 * @test
 * @summary java.rmi.server.LoaderHandler conformance: white-box structure + black-box behavior.
 * @run main LoaderHandlerTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import java.rmi.server.LoaderHandler;

public class LoaderHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.LoaderHandler");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        check(LoaderHandler.packagePrefix.equals("sun.rmi.server"), "packagePrefix == sun.rmi.server");
    }

    static void testBlackBox() throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        final Object sentinel = new Object();

        LoaderHandler lh = new LoaderHandler() {
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return Class.forName(name, false, loader);
            }
            public Class<?> loadClass(URL codebase, String name) throws ClassNotFoundException {
                return (codebase == null) ? loadClass(name) : null;
            }
            public Object getSecurityContext(ClassLoader cl) {
                return (cl == loader) ? sentinel : null;
            }
        };

        check(lh instanceof LoaderHandler, "anonymous instanceof LoaderHandler");
        check(lh.loadClass("java.lang.String") == String.class, "loadClass(String) loads String");
        check(lh.loadClass(null, "java.lang.String") == String.class, "loadClass(null, name) loads String");
        check(lh.loadClass(new URL("file:///tmp/"), "java.lang.String") == null, "loadClass(URL, name) sentinel");
        check(lh.getSecurityContext(loader) == sentinel, "getSecurityContext returns sentinel");
        check(lh.getSecurityContext(ClassLoader.getPlatformClassLoader()) == null, "getSecurityContext other loader null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
