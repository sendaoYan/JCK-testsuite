/*
 * @test
 * @summary com.sun.corba.se.impl.io.IIOPInputStream conformance: type shape via reflection (headless-safe).
 * @run main IIOPInputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IIOPInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.IIOPInputStream", false, IIOPInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.io.InputStreamHook"), "superclass com.sun.corba.se.impl.io.InputStreamHook");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 29, "public declared method count");
        check(mn.contains("available"), "method available");
        check(mn.contains("close"), "method close");
        check(mn.contains("enableResolveObjectDelegate"), "method enableResolveObjectDelegate");
        check(mn.contains("getSender"), "method getSender");
        check(mn.contains("getValueHandler"), "method getValueHandler");
        check(mn.contains("mark"), "method mark");
        check(mn.contains("markSupported"), "method markSupported");
        check(mn.contains("read"), "method read");
        check(mn.contains("readBoolean"), "method readBoolean");
        check(mn.contains("readByte"), "method readByte");
        check(mn.contains("readChar"), "method readChar");
        check(mn.contains("readDouble"), "method readDouble");
        check(mn.contains("readFloat"), "method readFloat");
        check(mn.contains("readFully"), "method readFully");
        check(mn.contains("readInt"), "method readInt");
        check(mn.contains("readLine"), "method readLine");
        check(mn.contains("readLong"), "method readLong");
        check(mn.contains("readObjectDelegate"), "method readObjectDelegate");
        check(mn.contains("readShort"), "method readShort");
        check(mn.contains("readUTF"), "method readUTF");
        check(mn.contains("readUnsignedByte"), "method readUnsignedByte");
        check(mn.contains("readUnsignedShort"), "method readUnsignedShort");
        check(mn.contains("registerValidation"), "method registerValidation");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("setSender"), "method setSender");
        check(mn.contains("setTestFVDFlag"), "method setTestFVDFlag");
        check(mn.contains("setValueHandler"), "method setValueHandler");
        check(mn.contains("simpleSkipObject"), "method simpleSkipObject");
        check(mn.contains("skipBytes"), "method skipBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
