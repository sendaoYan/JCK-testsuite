/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.net.sdp.SdpProvider conformance: class and members.
 * @run main SdpProviderTest
 */

public class SdpProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.sdp.SdpProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.sdp.SdpProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.NetHooks$Provider"), "superclass sun.net.NetHooks$Provider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("convertTcpToSdpIfMatch"), "declares method convertTcpToSdpIfMatch");
        check(mnames.contains("fail"), "declares method fail");
        check(mnames.contains("implBeforeTcpBind"), "declares method implBeforeTcpBind");
        check(mnames.contains("implBeforeTcpConnect"), "declares method implBeforeTcpConnect");
        check(mnames.contains("loadRulesFromFile"), "declares method loadRulesFromFile");
        check(mnames.contains("parsePortRange"), "declares method parsePortRange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
