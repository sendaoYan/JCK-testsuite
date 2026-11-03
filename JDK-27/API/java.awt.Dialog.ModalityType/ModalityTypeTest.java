/*
 * @test
 * @summary java.awt.Dialog.ModalityType conformance: white-box structure + black-box behavior.
 * @run main ModalityTypeTest
 */

import java.lang.reflect.*;

public class ModalityTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Dialog$ModalityType");
        check(c != null, "type loads: java.awt.Dialog.ModalityType");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants().length == 4, "four constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.Dialog.ModalityType[] all = java.awt.Dialog.ModalityType.values();
        check(all.length == 4, "values length 4");
        check(all[0] == java.awt.Dialog.ModalityType.MODELESS, "values[0] MODELESS");
        check(all[1] == java.awt.Dialog.ModalityType.DOCUMENT_MODAL, "values[1] DOCUMENT_MODAL");
        check(all[2] == java.awt.Dialog.ModalityType.APPLICATION_MODAL, "values[2] APPLICATION_MODAL");
        check(all[3] == java.awt.Dialog.ModalityType.TOOLKIT_MODAL, "values[3] TOOLKIT_MODAL");

        check(java.awt.Dialog.ModalityType.MODELESS.ordinal() == 0, "MODELESS ordinal 0");
        check(java.awt.Dialog.ModalityType.TOOLKIT_MODAL.ordinal() == 3, "TOOLKIT_MODAL ordinal 3");

        for (java.awt.Dialog.ModalityType t : all) {
            check(java.awt.Dialog.ModalityType.valueOf(t.name()) == t, "valueOf(name) roundtrip " + t);
            check(t.compareTo(t) == 0, "compareTo self 0");
        }
        check(java.awt.Dialog.ModalityType.valueOf("APPLICATION_MODAL")
                == java.awt.Dialog.ModalityType.APPLICATION_MODAL, "valueOf literal");
        check(java.awt.Dialog.ModalityType.MODELESS.name().equals("MODELESS"), "name()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
