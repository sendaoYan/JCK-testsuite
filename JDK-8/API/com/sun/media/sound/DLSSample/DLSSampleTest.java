/*
 * @test
 * @summary com.sun.media.sound.DLSSample conformance: type shape via reflection (headless-safe).
 * @run main DLSSampleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DLSSampleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.DLSSample", false, DLSSampleTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.DLSSample"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getInfo"), "method getInfo");
        check(names.contains("getData"), "method getData");
        check(names.contains("getDataBuffer"), "method getDataBuffer");
        check(names.contains("getFormat"), "method getFormat");
        check(names.contains("setFormat"), "method setFormat");
        check(names.contains("setData"), "method setData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
