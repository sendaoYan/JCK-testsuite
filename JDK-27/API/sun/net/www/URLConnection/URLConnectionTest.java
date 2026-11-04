/*
 * @test
 * @summary sun.net.www.URLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main URLConnectionTest
 */
public class URLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.URLConnection", false, URLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.URLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addRequestProperty"), "method addRequestProperty");
        check(hasMethod(c, "canCache"), "method canCache");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "ensureCanServeHeaders"), "method ensureCanServeHeaders");
        check(hasMethod(c, "getContentLength"), "method getContentLength");
        check(hasMethod(c, "getContentType"), "method getContentType");
        check(hasMethod(c, "getHeaderField"), "method getHeaderField");
        check(hasMethod(c, "getHeaderFieldKey"), "method getHeaderFieldKey");
        check(hasMethod(c, "getHeaderFields"), "method getHeaderFields");
        check(hasMethod(c, "getProperties"), "method getProperties");
        check(hasMethod(c, "getRequestProperties"), "method getRequestProperties");
        check(hasMethod(c, "getRequestProperty"), "method getRequestProperty");
        check(hasMethod(c, "isProxiedHost"), "method isProxiedHost");
        check(hasMethod(c, "setContentLength"), "method setContentLength");
        check(hasMethod(c, "setContentType"), "method setContentType");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setProxiedHost"), "method setProxiedHost");
        check(hasMethod(c, "setRequestProperty"), "method setRequestProperty");
        check(hasField(c, "contentLength"), "field contentLength");
        check(hasField(c, "contentType"), "field contentType");
        check(hasField(c, "headerFields"), "field headerFields");
        check(hasField(c, "properties"), "field properties");
        check(hasField(c, "proxiedHosts"), "field proxiedHosts");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
