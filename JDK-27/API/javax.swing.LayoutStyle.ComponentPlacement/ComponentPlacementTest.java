/*
 * @test
 * @summary javax.swing.LayoutStyle.ComponentPlacement conformance: type shape and supertypes.
 * @run main ComponentPlacementTest
 */

import java.lang.reflect.Modifier;

public class ComponentPlacementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutStyle$ComponentPlacement");
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
        check(ec != null && ec.length == 3, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("RELATED"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("UNRELATED"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("INDENT"), "enum constant 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
