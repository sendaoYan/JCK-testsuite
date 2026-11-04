/*
 * @test
 * @summary java.awt.Dialog.ModalExclusionType conformance: white-box structure + black-box behavior.
 * @run main ModalExclusionTypeTest
 */

import java.lang.reflect.*;

public class ModalExclusionTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Dialog$ModalExclusionType");
        check(c != null, "type loads: java.awt.Dialog.ModalExclusionType");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants().length == 3, "three constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.Dialog.ModalExclusionType[] all = java.awt.Dialog.ModalExclusionType.values();
        check(all.length == 3, "values length 3");
        check(all[0] == java.awt.Dialog.ModalExclusionType.NO_EXCLUDE, "values[0] NO_EXCLUDE");
        check(all[1] == java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE, "values[1] APPLICATION_EXCLUDE");
        check(all[2] == java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE, "values[2] TOOLKIT_EXCLUDE");

        check(java.awt.Dialog.ModalExclusionType.NO_EXCLUDE.ordinal() == 0, "NO_EXCLUDE ordinal 0");
        check(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE.ordinal() == 2, "TOOLKIT_EXCLUDE ordinal 2");

        for (java.awt.Dialog.ModalExclusionType t : all) {
            check(java.awt.Dialog.ModalExclusionType.valueOf(t.name()) == t, "valueOf roundtrip " + t);
        }
        check(java.awt.Dialog.ModalExclusionType.valueOf("APPLICATION_EXCLUDE")
                == java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE, "valueOf literal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
