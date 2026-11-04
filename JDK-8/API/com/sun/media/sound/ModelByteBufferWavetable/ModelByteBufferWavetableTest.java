/*
 * @test
 * @summary com.sun.media.sound.ModelByteBufferWavetable conformance: type shape via reflection (headless-safe).
 * @run main ModelByteBufferWavetableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelByteBufferWavetableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelByteBufferWavetable", false, ModelByteBufferWavetableTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelByteBufferWavetable"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "com.sun.media.sound.ModelWavetable"), "implements com.sun.media.sound.ModelWavetable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("set8BitExtensionBuffer"), "method set8BitExtensionBuffer");
        check(names.contains("get8BitExtensionBuffer"), "method get8BitExtensionBuffer");
        check(names.contains("getBuffer"), "method getBuffer");
        check(names.contains("getFormat"), "method getFormat");
        check(names.contains("openStream"), "method openStream");
        check(names.contains("getChannels"), "method getChannels");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
