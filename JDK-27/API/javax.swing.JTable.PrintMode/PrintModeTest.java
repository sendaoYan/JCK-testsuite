/*
 * @test
 * @summary javax.swing.JTable.PrintMode conformance: type shape and supertypes.
 * @run main PrintModeTest
 */

import java.lang.reflect.Modifier;

public class PrintModeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTable$PrintMode");
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
        check(ec != null && ec.length == 2, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("NORMAL"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("FIT_WIDTH"), "enum constant 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
