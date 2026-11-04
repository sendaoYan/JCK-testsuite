/*
 * @test
 * @summary com.sun.media.sound.ModelMappedInstrument conformance: type shape via reflection (headless-safe).
 * @run main ModelMappedInstrumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelMappedInstrumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelMappedInstrument", false, ModelMappedInstrumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelMappedInstrument"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.media.sound.ModelInstrument"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getData"), "method getData");
        check(names.contains("getPerformers"), "method getPerformers");
        check(names.contains("getDirector"), "method getDirector");
        check(names.contains("getChannelMixer"), "method getChannelMixer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
