/*
 * @test
 * @summary javax.swing.plaf.synth.SynthLookAndFeel conformance: type shape via reflection (headless-safe).
 * @run main SynthLookAndFeelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SynthLookAndFeelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthLookAndFeel", false, SynthLookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLookAndFeel"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setStyleFactory"), "method setStyleFactory");
        check(names.contains("getStyleFactory"), "method getStyleFactory");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("updateStyles"), "method updateStyles");
        check(names.contains("getRegion"), "method getRegion");
        check(names.contains("createUI"), "method createUI");
        check(names.contains("load"), "method load");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
