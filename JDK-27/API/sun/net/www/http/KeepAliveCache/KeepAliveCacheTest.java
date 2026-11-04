/*
 * @test
 * @summary sun.net.www.http.KeepAliveCache conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main KeepAliveCacheTest
 */
public class KeepAliveCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.KeepAliveCache", false, KeepAliveCacheTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.HashMap"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Runnable"), "interface java.lang.Runnable");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "run"), "method run");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "LIFETIME"), "field LIFETIME");
        check(hasField(c, "MAX_CONNECTIONS"), "field MAX_CONNECTIONS");
        check(hasField(c, "cacheLock"), "field cacheLock");
        check(hasField(c, "keepAliveProp"), "field keepAliveProp");
        check(hasField(c, "keepAliveTimer"), "field keepAliveTimer");
        check(hasField(c, "logger"), "field logger");
        check(hasField(c, "result"), "field result");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        check(hasField(c, "userKeepAliveProxy"), "field userKeepAliveProxy");
        check(hasField(c, "userKeepAliveServer"), "field userKeepAliveServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
