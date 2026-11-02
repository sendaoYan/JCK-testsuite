/*
 * @test
 * @summary com.sun.media.sound.ModelAbstractOscillator conformance: type shape via reflection (headless-safe).
 * @run main ModelAbstractOscillatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelAbstractOscillatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelAbstractOscillator", false, ModelAbstractOscillatorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelAbstractOscillator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "com.sun.media.sound.ModelOscillator"), "implements com.sun.media.sound.ModelOscillator");
        check(hasInterface(c, "com.sun.media.sound.ModelOscillatorStream"), "implements com.sun.media.sound.ModelOscillatorStream");
        check(hasInterface(c, "javax.sound.midi.Soundbank"), "implements javax.sound.midi.Soundbank");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("init"), "method init");
        check(names.contains("close"), "method close");
        check(names.contains("noteOff"), "method noteOff");
        check(names.contains("noteOn"), "method noteOn");
        check(names.contains("read"), "method read");
        check(names.contains("getChannel"), "method getChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
