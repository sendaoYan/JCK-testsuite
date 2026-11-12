/*
 * @test
 * @summary sun.net.www.protocol.http.ntlm.NTLMAuthentication conformance: class and members.
 * @run main NTLMAuthenticationTest
 */

public class NTLMAuthenticationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.ntlm.NTLMAuthentication", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.ntlm.NTLMAuthentication"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.AuthenticationInfo"), "superclass sun.net.www.protocol.http.AuthenticationInfo");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("buildType1Msg"), "declares method buildType1Msg");
        check(mnames.contains("buildType3Msg"), "declares method buildType3Msg");
        check(mnames.contains("getHeaderValue"), "declares method getHeaderValue");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("init0"), "declares method init0");
        check(mnames.contains("isAuthorizationStale"), "declares method isAuthorizationStale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
