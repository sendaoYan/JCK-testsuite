/*
 * @test
 * @summary javax.swing.text.Keymap conformance: type shape via reflection (headless-safe).
 * @run main KeymapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class KeymapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Keymap", false, KeymapTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getDefaultAction"), "method getDefaultAction");
        check(names.contains("setDefaultAction"), "method setDefaultAction");
        check(names.contains("getAction"), "method getAction");
        check(names.contains("getBoundKeyStrokes"), "method getBoundKeyStrokes");
        check(names.contains("getBoundActions"), "method getBoundActions");
        check(names.contains("getKeyStrokesForAction"), "method getKeyStrokesForAction");
        check(names.contains("isLocallyDefined"), "method isLocallyDefined");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
