/*
 * @test
 * @summary java.awt.FileDialog conformance: class reflection.
 * @run main FileDialogTest
 */

public class FileDialogTest {
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
        Class<?> c = Class.forName("java.awt.FileDialog");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Dialog"), "superclass java.awt.Dialog");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getMode"), "method getMode");
        check(hasMethod(c, "setMode"), "method setMode");
        check(hasMethod(c, "getDirectory"), "method getDirectory");
        check(hasMethod(c, "setDirectory"), "method setDirectory");
        check(hasMethod(c, "getFile"), "method getFile");
        check(hasMethod(c, "getFiles"), "method getFiles");
        check(hasMethod(c, "setFile"), "method setFile");
        check(hasMethod(c, "setMultipleMode"), "method setMultipleMode");
        check(hasMethod(c, "isMultipleMode"), "method isMultipleMode");
        check(hasMethod(c, "getFilenameFilter"), "method getFilenameFilter");
        check(hasMethod(c, "setFilenameFilter"), "method setFilenameFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
