/*
 * @test
 * @summary sun.net.TransferProtocolClient conformance: class and members.
 * @run main TransferProtocolClientTest
 */

public class TransferProtocolClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.TransferProtocolClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.TransferProtocolClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.NetworkClient"), "superclass sun.net.NetworkClient");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getResponseString"), "declares method getResponseString");
        check(mnames.contains("getResponseStrings"), "declares method getResponseStrings");
        check(mnames.contains("readServerResponse"), "declares method readServerResponse");
        check(mnames.contains("sendServer"), "declares method sendServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
