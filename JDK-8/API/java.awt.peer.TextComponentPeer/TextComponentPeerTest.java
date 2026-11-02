/*
 * @test
 * @summary java.awt.peer.TextComponentPeer conformance: interface reflection.
 * @run main TextComponentPeerTest
 */

public class TextComponentPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.TextComponentPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.ComponentPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setEditable"), "method setEditable");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "setCaretPosition"), "method setCaretPosition");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "getInputMethodRequests"), "method getInputMethodRequests");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
