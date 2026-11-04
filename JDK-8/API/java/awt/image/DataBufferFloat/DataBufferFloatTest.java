/*
 * @test
 * @summary java.awt.image.DataBufferFloat conformance: class reflection.
 * @run main DataBufferFloatTest
 */

public class DataBufferFloatTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferFloat");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        check(hasMethod(c, "getElemFloat"), "method getElemFloat");
        check(hasMethod(c, "setElemFloat"), "method setElemFloat");
        check(hasMethod(c, "getElemDouble"), "method getElemDouble");
        check(hasMethod(c, "setElemDouble"), "method setElemDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
