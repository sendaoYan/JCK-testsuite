/*
 * @test
 * @summary sun.jvm.hotspot.utilities.Unsigned5 conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main Unsigned5Test
 */
import java.lang.reflect.*;

public class Unsigned5Test {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.Unsigned5", false, Unsigned5Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "base"), "method base");
        check(hasMethod(c, "getByte"), "method getByte");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "dump"), "method dump");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "dumpOn"), "method dumpOn");
        check(hasMethod(c, "decodeSign"), "method decodeSign");
        check(hasMethod(c, "readUint"), "method readUint");
        check(hasMethod(c, "encodeSign"), "method encodeSign");
        check(hasField(c, "LogBitsPerByte"), "field LogBitsPerByte");
        check(hasField(c, "BitsPerByte"), "field BitsPerByte");
        check(hasField(c, "MAX_LENGTH"), "field MAX_LENGTH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
