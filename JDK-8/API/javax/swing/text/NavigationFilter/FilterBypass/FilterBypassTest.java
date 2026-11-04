/*
 * @test
 * @summary javax.swing.text.NavigationFilter.FilterBypass conformance: type shape via reflection (headless-safe).
 * @run main FilterBypassTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FilterBypassTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.NavigationFilter$FilterBypass", false, FilterBypassTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getCaret"), "method getCaret");
        check(names.contains("setDot"), "method setDot");
        check(names.contains("moveDot"), "method moveDot");

        // a concrete bypass drives the caret it was created for
        final javax.swing.text.DefaultCaret caret = new javax.swing.text.DefaultCaret();
        javax.swing.text.NavigationFilter.FilterBypass fb = new javax.swing.text.NavigationFilter.FilterBypass() {
            public javax.swing.text.Caret getCaret() { return caret; }
            public void setDot(int dot, javax.swing.text.Position.Bias bias) {
                log += "set:" + dot + ":" + bias + ";";
            }
            public void moveDot(int dot, javax.swing.text.Position.Bias bias) {
                log += "move:" + dot + ":" + bias + ";";
            }
            String log = "";
        };
        check(fb.getCaret() == caret, "getCaret returns the caret");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
