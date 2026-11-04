/*
 * @test
 * @summary sun.net.sdp.SdpSupport conformance: class and members.
 * @run main SdpSupportTest
 */

public class SdpSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.sdp.SdpSupport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.sdp.SdpSupport"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("convert0"), "declares method convert0");
        check(mnames.contains("convertSocket"), "declares method convertSocket");
        check(mnames.contains("create0"), "declares method create0");
        check(mnames.contains("createSocket"), "declares method createSocket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
