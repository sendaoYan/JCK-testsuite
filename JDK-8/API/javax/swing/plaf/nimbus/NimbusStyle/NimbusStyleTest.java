/*
 * @test
 * @summary javax.swing.plaf.nimbus.NimbusStyle conformance: type shape via reflection (headless-safe).
 * @run main NimbusStyleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NimbusStyleTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.NimbusStyle", false, NimbusStyleTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthStyle"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("installDefaults"), "method installDefaults");
        check(names.contains("getInsets"), "method getInsets");
        check(names.contains("getPainter"), "method getPainter");
        check(names.contains("isOpaque"), "method isOpaque");
        check(names.contains("get"), "method get");
        check(names.contains("getBackgroundPainter"), "method getBackgroundPainter");
        check(names.contains("getForegroundPainter"), "method getForegroundPainter");
        check(names.contains("getBorderPainter"), "method getBorderPainter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
