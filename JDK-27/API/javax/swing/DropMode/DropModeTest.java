/*
 * @test
 * @summary javax.swing.DropMode conformance: white-box structure + black-box behavior.
 * @run main DropModeTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DropMode;

public class DropModeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DropMode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 8, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("USE_SELECTION"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("ON"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("INSERT"), "enum constant 2");
        check(((Enum<?>) ec[3]).name().equals("INSERT_ROWS"), "enum constant 3");
        check(((Enum<?>) ec[4]).name().equals("INSERT_COLS"), "enum constant 4");
        check(((Enum<?>) ec[5]).name().equals("ON_OR_INSERT"), "enum constant 5");
        check(((Enum<?>) ec[6]).name().equals("ON_OR_INSERT_ROWS"), "enum constant 6");
        check(((Enum<?>) ec[7]).name().equals("ON_OR_INSERT_COLS"), "enum constant 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(DropMode.values().length == 8, "values length");
        check(DropMode.valueOf("ON") == DropMode.ON, "valueOf ON");
        check(DropMode.valueOf("INSERT_ROWS") == DropMode.INSERT_ROWS, "valueOf INSERT_ROWS");
        check("USE_SELECTION".equals(DropMode.USE_SELECTION.name()), "name");
        check(DropMode.ON.ordinal() == 1, "ordinal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
