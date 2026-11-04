/*
 * @test
 * @summary java.awt.peer.ContainerPeer conformance: interface reflection.
 * @run main ContainerPeerTest
 */

public class ContainerPeerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.peer.ContainerPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.ComponentPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getInsets"), "method getInsets");
        check(hasMethod(c, "beginValidate"), "method beginValidate");
        check(hasMethod(c, "endValidate"), "method endValidate");
        check(hasMethod(c, "beginLayout"), "method beginLayout");
        check(hasMethod(c, "endLayout"), "method endLayout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
