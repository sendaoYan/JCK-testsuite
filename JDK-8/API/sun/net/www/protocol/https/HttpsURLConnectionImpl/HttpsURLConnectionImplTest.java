/*
 * @test
 * @summary sun.net.www.protocol.https.HttpsURLConnectionImpl conformance: class and members.
 * @run main HttpsURLConnectionImplTest
 */

public class HttpsURLConnectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.https.HttpsURLConnectionImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.https.HttpsURLConnectionImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.net.ssl.HttpsURLConnection"), "superclass javax.net.ssl.HttpsURLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addRequestProperty"), "declares method addRequestProperty");
        check(mnames.contains("checkURL"), "declares method checkURL");
        check(mnames.contains("connect"), "declares method connect");
        check(mnames.contains("disconnect"), "declares method disconnect");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("finalize"), "declares method finalize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
