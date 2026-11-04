/*
 * @test
 * @summary javax.swing.text.AbstractDocument.BranchElement conformance: type shape via reflection (headless-safe).
 * @run main BranchElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BranchElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$BranchElement", false, BranchElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$AbstractElement"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("positionToElement"), "method positionToElement");
        check(names.contains("replace"), "method replace");
        check(names.contains("toString"), "method toString");
        check(names.contains("getName"), "method getName");
        check(names.contains("getStartOffset"), "method getStartOffset");
        check(names.contains("getEndOffset"), "method getEndOffset");
        check(names.contains("getElement"), "method getElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
