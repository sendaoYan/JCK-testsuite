/*
 * @test
 * @summary java.awt.peer.TextFieldPeer conformance: interface reflection.
 * @run main TextFieldPeerTest
 */

public class TextFieldPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.TextFieldPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.TextComponentPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setEchoChar"), "method setEchoChar");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
