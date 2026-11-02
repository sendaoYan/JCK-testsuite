/*
 * @test
 * @summary sun.nio.ch.sctp.ResultContainer conformance: class and members.
 * @run main ResultContainerTest
 */

public class ResultContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.ResultContainer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.ResultContainer"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clear"), "declares method clear");
        check(mnames.contains("getAssociationChanged"), "declares method getAssociationChanged");
        check(mnames.contains("getMessageInfo"), "declares method getMessageInfo");
        check(mnames.contains("getPeerAddressChanged"), "declares method getPeerAddressChanged");
        check(mnames.contains("getSendFailed"), "declares method getSendFailed");
        check(mnames.contains("getShutdown"), "declares method getShutdown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
