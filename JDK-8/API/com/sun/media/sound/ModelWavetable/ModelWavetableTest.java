/*
 * @test
 * @summary com.sun.media.sound.ModelWavetable conformance: type shape via reflection (headless-safe).
 * @run main ModelWavetableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelWavetableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelWavetable", false, ModelWavetableTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelWavetable"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(hasInterface(c, "com.sun.media.sound.ModelOscillator"), "implements com.sun.media.sound.ModelOscillator");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("openStream"), "method openStream");
        check(names.contains("getLoopLength"), "method getLoopLength");
        check(names.contains("getLoopStart"), "method getLoopStart");
        check(names.contains("getLoopType"), "method getLoopType");
        check(names.contains("getPitchcorrection"), "method getPitchcorrection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
