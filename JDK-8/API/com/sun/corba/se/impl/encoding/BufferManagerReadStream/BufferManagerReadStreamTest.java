/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.BufferManagerReadStream conformance: type shape via reflection (headless-safe).
 * @run main BufferManagerReadStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BufferManagerReadStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.BufferManagerReadStream", false, BufferManagerReadStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.encoding.BufferManagerRead"), "interface com.sun.corba.se.impl.encoding.BufferManagerRead");
        check(in.contains("com.sun.corba.se.impl.encoding.MarkAndResetHandler"), "interface com.sun.corba.se.impl.encoding.MarkAndResetHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("cancelProcessing"), "method cancelProcessing");
        check(mn.contains("close"), "method close");
        check(mn.contains("fragmentationOccured"), "method fragmentationOccured");
        check(mn.contains("getMarkAndResetHandler"), "method getMarkAndResetHandler");
        check(mn.contains("init"), "method init");
        check(mn.contains("mark"), "method mark");
        check(mn.contains("processFragment"), "method processFragment");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("underflow"), "method underflow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
