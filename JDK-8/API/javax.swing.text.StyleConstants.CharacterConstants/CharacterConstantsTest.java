/*
 * @test
 * @summary javax.swing.text.StyleConstants.CharacterConstants conformance: type shape via reflection (headless-safe).
 * @run main CharacterConstantsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CharacterConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$CharacterConstants", false, CharacterConstantsTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyleConstants"), "superclass");
        check(hasInterface(c, "javax.swing.text.AttributeSet$CharacterAttribute"), "implements javax.swing.text.AttributeSet$CharacterAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
