/*
 * @test
 * @summary sun.jvm.hotspot.utilities.Bits conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main BitsTest
 */
import java.lang.reflect.*;

public class BitsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.Bits", false, BitsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isSetNthBit"), "method isSetNthBit");
        check(hasMethod(c, "setNthBit"), "method setNthBit");
        check(hasMethod(c, "clearNthBit"), "method clearNthBit");
        check(hasMethod(c, "roundTo"), "method roundTo");
        check(hasMethod(c, "maskBits"), "method maskBits");
        check(hasMethod(c, "rightNBits"), "method rightNBits");
        check(hasMethod(c, "nthBit"), "method nthBit");
        check(hasMethod(c, "maskBitsLong"), "method maskBitsLong");
        check(hasMethod(c, "setBits"), "method setBits");
        check(hasMethod(c, "clearBits"), "method clearBits");
        check(hasField(c, "AllBits"), "field AllBits");
        check(hasField(c, "NoBits"), "field NoBits");
        check(hasField(c, "OneBit"), "field OneBit");
        check(hasField(c, "BitsPerByte"), "field BitsPerByte");
        check(hasField(c, "BitsPerInt"), "field BitsPerInt");
        check(hasField(c, "LogBytesPerInt"), "field LogBytesPerInt");
        check(hasField(c, "LogBytesPerLong"), "field LogBytesPerLong");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
