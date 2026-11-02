/*
 * @test
 * @summary sun.net.www.protocol.jar.JarURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.jar:+open
 * @run main JarURLConnectionTest
 */
public class JarURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.jar.JarURLConnection", false, JarURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.JarURLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addRequestProperty"), "method addRequestProperty");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "getAllowUserInteraction"), "method getAllowUserInteraction");
        check(hasMethod(c, "getContent"), "method getContent");
        check(hasMethod(c, "getContentLength"), "method getContentLength");
        check(hasMethod(c, "getContentLengthLong"), "method getContentLengthLong");
        check(hasMethod(c, "getContentType"), "method getContentType");
        check(hasMethod(c, "getDefaultUseCaches"), "method getDefaultUseCaches");
        check(hasMethod(c, "getHeaderField"), "method getHeaderField");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getJarEntry"), "method getJarEntry");
        check(hasMethod(c, "getJarFile"), "method getJarFile");
        check(hasMethod(c, "getPermission"), "method getPermission");
        check(hasMethod(c, "getRequestProperties"), "method getRequestProperties");
        check(hasMethod(c, "getRequestProperty"), "method getRequestProperty");
        check(hasMethod(c, "getUseCaches"), "method getUseCaches");
        check(hasMethod(c, "setAllowUserInteraction"), "method setAllowUserInteraction");
        check(hasMethod(c, "setDefaultUseCaches"), "method setDefaultUseCaches");
        check(hasMethod(c, "setIfModifiedSince"), "method setIfModifiedSince");
        check(hasMethod(c, "setRequestProperty"), "method setRequestProperty");
        check(hasMethod(c, "setUseCaches"), "method setUseCaches");
        check(hasField(c, "contentType"), "field contentType");
        check(hasField(c, "factory"), "field factory");
        check(hasField(c, "jarEntry"), "field jarEntry");
        check(hasField(c, "jarFile"), "field jarFile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
