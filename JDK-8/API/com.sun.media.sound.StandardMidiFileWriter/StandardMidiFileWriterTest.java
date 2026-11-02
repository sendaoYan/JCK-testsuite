/*
 * @test
 * @summary com.sun.media.sound.StandardMidiFileWriter conformance: type shape via reflection (headless-safe).
 * @run main StandardMidiFileWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StandardMidiFileWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.StandardMidiFileWriter", false, StandardMidiFileWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.StandardMidiFileWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.spi.MidiFileWriter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getMidiFileTypes"), "method getMidiFileTypes");
        check(names.contains("isFileTypeSupported"), "method isFileTypeSupported");
        check(names.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
