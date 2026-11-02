/*
 * @test
 * @summary java.awt.dnd.DropTarget conformance: class reflection.
 * @run main DropTargetTest
 */

public class DropTargetTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTarget");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.dnd.DropTargetListener", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setComponent"), "method setComponent");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "setDefaultActions"), "method setDefaultActions");
        check(hasMethod(c, "getDefaultActions"), "method getDefaultActions");
        check(hasMethod(c, "setActive"), "method setActive");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "addDropTargetListener"), "method addDropTargetListener");
        check(hasMethod(c, "removeDropTargetListener"), "method removeDropTargetListener");
        check(hasMethod(c, "dragEnter"), "method dragEnter");
        check(hasMethod(c, "dragOver"), "method dragOver");
        check(hasMethod(c, "dropActionChanged"), "method dropActionChanged");
        check(hasMethod(c, "dragExit"), "method dragExit");
        check(hasMethod(c, "drop"), "method drop");
        check(hasMethod(c, "getFlavorMap"), "method getFlavorMap");
        check(hasMethod(c, "setFlavorMap"), "method setFlavorMap");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "getDropTargetContext"), "method getDropTargetContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
