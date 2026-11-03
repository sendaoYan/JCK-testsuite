/*
 * @test
 * @summary java.awt.Dialog conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DialogTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class DialogTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Dialog");
        check(c != null, "type loads: java.awt.Dialog");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Window.class, "extends Window");
        for (String name : new String[]{"isModal", "setModal", "getModalityType",
                "setModalityType", "setTitle", "getTitle", "setResizable", "isResizable",
                "setUndecorated", "isUndecorated", "setVisible"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(Dialog.DEFAULT_MODALITY_TYPE == Dialog.ModalityType.APPLICATION_MODAL,
              "DEFAULT_MODALITY_TYPE is APPLICATION_MODAL");

        Frame owner = new Frame("owner");
        owner.setVisible(true);
        try {
            // modeless default
            Dialog d = new Dialog(owner, "modeless");
            check(!d.isModal(), "default not modal");
            check(d.getModalityType() == Dialog.ModalityType.MODELESS, "default MODELESS");
            check("modeless".equals(d.getTitle()), "constructor title");
            check(d.isResizable(), "resizable default true");
            check(!d.isUndecorated(), "decorated default");

            d.setTitle("t2");
            check("t2".equals(d.getTitle()), "setTitle/getTitle");
            d.setResizable(false);
            check(!d.isResizable(), "setResizable(false)");
            d.setUndecorated(true);
            check(d.isUndecorated(), "setUndecorated(true)");

            // modeless visible path only (modal never shown here)
            d.setSize(100, 80);
            d.setVisible(true);
            check(d.isDisplayable(), "modeless displayable after setVisible");
            check(d.isShowing(), "modeless showing");
            d.setVisible(false);
            check(!d.isShowing(), "modeless hidden");
            d.dispose();

            // modal via boolean constructor
            Dialog modal = new Dialog(owner, "modal", true);
            check(modal.isModal(), "boolean constructor modal");
            check(modal.getModalityType() == Dialog.ModalityType.APPLICATION_MODAL,
                  "modal maps to APPLICATION_MODAL");
            modal.dispose();

            // modality type constructor
            Dialog doc = new Dialog(owner, "doc", Dialog.ModalityType.DOCUMENT_MODAL);
            check(doc.isModal(), "DOCUMENT_MODAL is modal");
            check(doc.getModalityType() == Dialog.ModalityType.DOCUMENT_MODAL, "DOCUMENT_MODAL type");
            doc.dispose();

            // setModal / setModalityType flags
            Dialog d2 = new Dialog(owner, "x");
            d2.setModal(true);
            check(d2.isModal(), "setModal(true)");
            check(d2.getModalityType() == Dialog.ModalityType.APPLICATION_MODAL,
                  "setModal(true) sets APPLICATION_MODAL");
            d2.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
            check(d2.getModalityType() == Dialog.ModalityType.TOOLKIT_MODAL, "setModalityType TOOLKIT_MODAL");
            check(d2.isModal(), "TOOLKIT_MODAL is modal");
            d2.setModal(false);
            check(!d2.isModal(), "setModal(false)");
            d2.dispose();
        } finally {
            owner.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
