/*
 * @test
 * @summary java.awt.peer.ScrollPanePeer conformance: interface reflection.
 * @run main ScrollPanePeerTest
 */

public class ScrollPanePeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.ScrollPanePeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.ContainerPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getHScrollbarHeight"), "method getHScrollbarHeight");
        check(hasMethod(c, "getVScrollbarWidth"), "method getVScrollbarWidth");
        check(hasMethod(c, "setScrollPosition"), "method setScrollPosition");
        check(hasMethod(c, "childResized"), "method childResized");
        check(hasMethod(c, "setUnitIncrement"), "method setUnitIncrement");
        check(hasMethod(c, "setValue"), "method setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
