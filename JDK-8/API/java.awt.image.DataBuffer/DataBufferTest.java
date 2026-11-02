/*
 * @test
 * @summary java.awt.image.DataBuffer conformance: abstract class reflection.
 * @run main DataBufferTest
 */

public class DataBufferTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBuffer");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDataTypeSize"), "method getDataTypeSize");
        check(hasMethod(c, "getDataType"), "method getDataType");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getOffsets"), "method getOffsets");
        check(hasMethod(c, "getNumBanks"), "method getNumBanks");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        check(hasMethod(c, "getElemFloat"), "method getElemFloat");
        check(hasMethod(c, "setElemFloat"), "method setElemFloat");
        check(hasMethod(c, "getElemDouble"), "method getElemDouble");
        check(hasMethod(c, "setElemDouble"), "method setElemDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
