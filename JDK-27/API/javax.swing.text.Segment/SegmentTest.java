/*
 * @test
 * @summary javax.swing.text.Segment conformance: type shape and supertypes.
 * @run main SegmentTest
 */

import java.lang.reflect.Modifier;

public class SegmentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Segment");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.text.CharacterIterator"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.lang.CharSequence"), "interface 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
