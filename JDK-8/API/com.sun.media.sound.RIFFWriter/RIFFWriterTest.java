/*
 * @test
 * @summary com.sun.media.sound.RIFFWriter conformance: type shape via reflection (headless-safe).
 * @run main RIFFWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RIFFWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.RIFFWriter", false, RIFFWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.RIFFWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("seek"), "method seek");
        check(names.contains("getFilePointer"), "method getFilePointer");
        check(names.contains("setWriteOverride"), "method setWriteOverride");
        check(names.contains("getWriteOverride"), "method getWriteOverride");
        check(names.contains("close"), "method close");
        check(names.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
