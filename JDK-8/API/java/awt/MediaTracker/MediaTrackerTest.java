/*
 * @test
 * @summary java.awt.MediaTracker conformance: class reflection.
 * @run main MediaTrackerTest
 */

public class MediaTrackerTest {
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
        Class<?> c = Class.forName("java.awt.MediaTracker");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addImage"), "method addImage");
        check(hasMethod(c, "checkAll"), "method checkAll");
        check(hasMethod(c, "isErrorAny"), "method isErrorAny");
        check(hasMethod(c, "getErrorsAny"), "method getErrorsAny");
        check(hasMethod(c, "waitForAll"), "method waitForAll");
        check(hasMethod(c, "statusAll"), "method statusAll");
        check(hasMethod(c, "checkID"), "method checkID");
        check(hasMethod(c, "isErrorID"), "method isErrorID");
        check(hasMethod(c, "getErrorsID"), "method getErrorsID");
        check(hasMethod(c, "waitForID"), "method waitForID");
        check(hasMethod(c, "statusID"), "method statusID");
        check(hasMethod(c, "removeImage"), "method removeImage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
