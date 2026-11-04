/*
 * @test
 * @summary java.awt.datatransfer.Transferable conformance: interface reflection.
 * @run main TransferableTest
 */

public class TransferableTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.Transferable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTransferDataFlavors"), "method getTransferDataFlavors");
        check(hasMethod(c, "isDataFlavorSupported"), "method isDataFlavorSupported");
        check(hasMethod(c, "getTransferData"), "method getTransferData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
