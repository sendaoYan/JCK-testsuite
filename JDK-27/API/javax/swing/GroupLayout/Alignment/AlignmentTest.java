/*
 * @test
 * @summary javax.swing.GroupLayout.Alignment conformance: type shape and supertypes.
 * @run main AlignmentTest
 */

import java.lang.reflect.Modifier;

public class AlignmentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.GroupLayout$Alignment");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == true, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 4, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("LEADING"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("TRAILING"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("CENTER"), "enum constant 2");
        check(((Enum<?>) ec[3]).name().equals("BASELINE"), "enum constant 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
