/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.BufferManagerRead conformance: type shape via reflection (headless-safe).
 * @run main BufferManagerReadTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BufferManagerReadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.BufferManagerRead", false, BufferManagerReadTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("cancelProcessing"), "method cancelProcessing");
        check(mn.contains("close"), "method close");
        check(mn.contains("getMarkAndResetHandler"), "method getMarkAndResetHandler");
        check(mn.contains("init"), "method init");
        check(mn.contains("processFragment"), "method processFragment");
        check(mn.contains("underflow"), "method underflow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
