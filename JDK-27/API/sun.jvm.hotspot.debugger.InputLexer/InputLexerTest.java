/*
 * @test
 * @summary sun.jvm.hotspot.debugger.InputLexer conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main InputLexerTest
 */
import java.lang.reflect.*;

public class InputLexerTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.InputLexer", false, InputLexerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "parseInt"), "method parseInt");
        check(hasMethod(c, "parseBoolean"), "method parseBoolean");
        check(hasMethod(c, "parseLong"), "method parseLong");
        check(hasMethod(c, "readByte"), "method readByte");
        check(hasMethod(c, "readBytes"), "method readBytes");
        check(hasMethod(c, "readChar"), "method readChar");
        check(hasMethod(c, "parseAddress"), "method parseAddress");
        check(hasMethod(c, "skipByte"), "method skipByte");
        check(hasMethod(c, "readUnsignedInt"), "method readUnsignedInt");
        check(hasMethod(c, "readByteString"), "method readByteString");
        check(hasMethod(c, "readCharString"), "method readCharString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
