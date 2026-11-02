/*
 * @test
 * @summary sun.jvm.hotspot.code.CompressedReadStream conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main CompressedReadStreamTest
 */
import java.lang.reflect.*;

public class CompressedReadStreamTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.CompressedReadStream", false, CompressedReadStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.code.CompressedStream".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "readInt"), "method readInt");
        check(hasMethod(c, "dump"), "method dump");
        check(hasMethod(c, "readLong"), "method readLong");
        check(hasMethod(c, "readByte"), "method readByte");
        check(hasMethod(c, "readShort"), "method readShort");
        check(hasMethod(c, "readChar"), "method readChar");
        check(hasMethod(c, "readFloat"), "method readFloat");
        check(hasMethod(c, "readBoolean"), "method readBoolean");
        check(hasMethod(c, "readDouble"), "method readDouble");
        check(hasMethod(c, "dumpOn"), "method dumpOn");
        check(hasMethod(c, "readSignedInt"), "method readSignedInt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
