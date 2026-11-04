/*
 * @test
 * @summary com.sun.media.sound.ModelStandardTransform conformance: type shape via reflection (headless-safe).
 * @run main ModelStandardTransformTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelStandardTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelStandardTransform", false, ModelStandardTransformTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelStandardTransform"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "com.sun.media.sound.ModelTransform"), "implements com.sun.media.sound.ModelTransform");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("transform"), "method transform");
        check(names.contains("getDirection"), "method getDirection");
        check(names.contains("setDirection"), "method setDirection");
        check(names.contains("getPolarity"), "method getPolarity");
        check(names.contains("setPolarity"), "method setPolarity");
        check(names.contains("getTransform"), "method getTransform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
