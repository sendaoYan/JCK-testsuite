/*
 * @test
 * @summary com.sun.corba.se.impl.io.IIOPOutputStream conformance: type shape via reflection (headless-safe).
 * @run main IIOPOutputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IIOPOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.IIOPOutputStream", false, IIOPOutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.io.OutputStreamHook"), "superclass com.sun.corba.se.impl.io.OutputStreamHook");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 19, "public declared method count");
        check(mn.contains("close"), "method close");
        check(mn.contains("defaultWriteObjectDelegate"), "method defaultWriteObjectDelegate");
        check(mn.contains("enableReplaceObjectDelegate"), "method enableReplaceObjectDelegate");
        check(mn.contains("flush"), "method flush");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("simpleWriteObject"), "method simpleWriteObject");
        check(mn.contains("write"), "method write");
        check(mn.contains("writeBoolean"), "method writeBoolean");
        check(mn.contains("writeByte"), "method writeByte");
        check(mn.contains("writeBytes"), "method writeBytes");
        check(mn.contains("writeChar"), "method writeChar");
        check(mn.contains("writeChars"), "method writeChars");
        check(mn.contains("writeDouble"), "method writeDouble");
        check(mn.contains("writeFloat"), "method writeFloat");
        check(mn.contains("writeInt"), "method writeInt");
        check(mn.contains("writeLong"), "method writeLong");
        check(mn.contains("writeObjectOverride"), "method writeObjectOverride");
        check(mn.contains("writeShort"), "method writeShort");
        check(mn.contains("writeUTF"), "method writeUTF");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
