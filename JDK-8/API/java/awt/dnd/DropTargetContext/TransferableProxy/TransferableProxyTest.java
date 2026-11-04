/*
 * @test
 * @summary java.awt.dnd.DropTargetContext.TransferableProxy conformance: class reflection.
 * @run main TransferableProxyTest
 */

public class TransferableProxyTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetContext$TransferableProxy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.datatransfer.Transferable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getTransferDataFlavors"), "method getTransferDataFlavors");
        check(hasMethod(c, "isDataFlavorSupported"), "method isDataFlavorSupported");
        check(hasMethod(c, "getTransferData"), "method getTransferData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
