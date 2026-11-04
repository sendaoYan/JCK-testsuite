/*
 * @test
 * @summary sun.rmi.server.Activation.ActivationSystemImpl_Stub conformance: class and members.
 * @run main ActivationSystemImpl_StubTest
 */

public class ActivationSystemImpl_StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.Activation$ActivationSystemImpl_Stub", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.Activation$ActivationSystemImpl_Stub"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RemoteStub"), "superclass java.rmi.server.RemoteStub");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.activation.ActivationSystem"), "interface java.rmi.activation.ActivationSystem");
        check(ifaces.contains("java.rmi.Remote"), "interface java.rmi.Remote");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("activeGroup"), "declares method activeGroup");
        check(mnames.contains("getActivationDesc"), "declares method getActivationDesc");
        check(mnames.contains("getActivationGroupDesc"), "declares method getActivationGroupDesc");
        check(mnames.contains("registerGroup"), "declares method registerGroup");
        check(mnames.contains("registerObject"), "declares method registerObject");
        check(mnames.contains("setActivationDesc"), "declares method setActivationDesc");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
