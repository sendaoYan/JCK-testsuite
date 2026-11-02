/*
 * @test
 * @summary java.awt.peer.ListPeer conformance: interface reflection.
 * @run main ListPeerTest
 */

public class ListPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.ListPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.ComponentPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getSelectedIndexes"), "method getSelectedIndexes");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "delItems"), "method delItems");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "deselect"), "method deselect");
        check(hasMethod(c, "makeVisible"), "method makeVisible");
        check(hasMethod(c, "setMultipleMode"), "method setMultipleMode");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
