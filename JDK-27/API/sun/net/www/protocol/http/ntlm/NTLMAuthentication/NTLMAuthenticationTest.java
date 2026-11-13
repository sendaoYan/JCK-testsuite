/*
 * @test
 * @summary sun.net.www.protocol.http.ntlm.NTLMAuthentication conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http.ntlm:+open
 * @run main NTLMAuthenticationTest
 */
public class NTLMAuthenticationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.ntlm.NTLMAuthentication", false, NTLMAuthenticationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.AuthenticationInfo"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getHeaderValue"), "method getHeaderValue");
        check(hasMethod(c, "isAuthorizationStale"), "method isAuthorizationStale");
        check(hasMethod(c, "isTrustedSite"), "method isTrustedSite");
        check(hasMethod(c, "setHeaders"), "method setHeaders");
        check(hasMethod(c, "supportsPreemptiveAuthorization"), "method supportsPreemptiveAuthorization");
        check(hasMethod(c, "supportsTransparentAuth"), "method supportsTransparentAuth");
        check(hasMethod(c, "useAuthCache"), "method useAuthCache");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            // The exact declared shape of an internal class is a property of the
            // platform build (member sets differ between the Linux and Windows
            // builds), so only the portable prefix above is asserted here.
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "NTLMAuthCallback"), "field NTLMAuthCallback");
        check(hasField(c, "client"), "field client");
        check(hasField(c, "defaultDomain"), "field defaultDomain");
        check(hasField(c, "hostname"), "field hostname");
        check(hasField(c, "ntlmCache"), "field ntlmCache");
        check(hasField(c, "pw"), "field pw");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
