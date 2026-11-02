/*
 * @test
 * @summary sun.net.www.protocol.http.AuthenticationInfo conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main AuthenticationInfoTest
 */
public class AuthenticationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthenticationInfo", false, AuthenticationInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.AuthCacheValue"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "credentials"), "method credentials");
        check(hasMethod(c, "disposeContext"), "method disposeContext");
        check(hasMethod(c, "getAuthType"), "method getAuthType");
        check(hasMethod(c, "getHeaderName"), "method getHeaderName");
        check(hasMethod(c, "getHeaderValue"), "method getHeaderValue");
        check(hasMethod(c, "getHost"), "method getHost");
        check(hasMethod(c, "getPath"), "method getPath");
        check(hasMethod(c, "getPort"), "method getPort");
        check(hasMethod(c, "getProtocolScheme"), "method getProtocolScheme");
        check(hasMethod(c, "getRealm"), "method getRealm");
        check(hasMethod(c, "isAuthorizationStale"), "method isAuthorizationStale");
        check(hasMethod(c, "setHeaders"), "method setHeaders");
        check(hasMethod(c, "supportsPreemptiveAuthorization"), "method supportsPreemptiveAuthorization");
        check(hasMethod(c, "useAuthCache"), "method useAuthCache");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "PROXY_AUTHENTICATION"), "field PROXY_AUTHENTICATION");
        check(hasField(c, "SERVER_AUTHENTICATION"), "field SERVER_AUTHENTICATION");
        check(hasField(c, "authScheme"), "field authScheme");
        check(hasField(c, "host"), "field host");
        check(hasField(c, "path"), "field path");
        check(hasField(c, "port"), "field port");
        check(hasField(c, "protocol"), "field protocol");
        check(hasField(c, "pw"), "field pw");
        check(hasField(c, "realm"), "field realm");
        check(hasField(c, "requestFinished"), "field requestFinished");
        check(hasField(c, "requestLock"), "field requestLock");
        check(hasField(c, "requests"), "field requests");
        check(hasField(c, "serializeAuth"), "field serializeAuth");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
