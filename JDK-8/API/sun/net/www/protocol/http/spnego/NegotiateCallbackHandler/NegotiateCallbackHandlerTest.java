/*
 * @test
 * @summary sun.net.www.protocol.http.spnego.NegotiateCallbackHandler conformance: class and members.
 * @run main NegotiateCallbackHandlerTest
 */

public class NegotiateCallbackHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.spnego.NegotiateCallbackHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.spnego.NegotiateCallbackHandler"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("javax.security.auth.callback.CallbackHandler"), "interface javax.security.auth.callback.CallbackHandler");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAnswer"), "declares method getAnswer");
        check(mnames.contains("handle"), "declares method handle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
