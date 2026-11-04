/*
 * @test
 * @summary sun.net.www.protocol.http.AuthCacheImpl conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main AuthCacheImplTest
 */
public class AuthCacheImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheImpl", false, AuthCacheImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.net.www.protocol.http.AuthCache"), "interface sun.net.www.protocol.http.AuthCache");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getAuthCacheFor"), "method getAuthCacheFor");
        check(hasMethod(c, "getDefault"), "method getDefault");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "setMap"), "method setMap");
        check(hasField(c, "caches"), "field caches");
        check(hasField(c, "hashtable"), "field hashtable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
