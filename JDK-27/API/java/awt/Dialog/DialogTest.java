/*
 * @test
 * @summary java.awt.Dialog conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DialogTest
 */

import java.awt.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class DialogTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected " + ex.getSimpleName() + ": " + msg);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), msg + " -> " + t.getClass().getSimpleName());
        }
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
        check(c.getConstructors().length == 14, "14 public constructors");
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
            check(d.getLayout() instanceof BorderLayout, "default layout BorderLayout");

            d.setTitle("t2");
            check("t2".equals(d.getTitle()), "setTitle/getTitle");
            d.setResizable(false);
            check(!d.isResizable(), "setResizable(false)");
            d.setUndecorated(true);
            d.setUndecorated(false);
            check(!d.isUndecorated(), "setUndecorated(false)");

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
            check(d2.getModalityType() == Dialog.ModalityType.MODELESS,
                  "setModal(false) resets to MODELESS");
            d2.dispose();
        } finally {
            owner.dispose();
        }
    }

    // ---- Part 3: full constructor matrix ----
    static void testConstructorMatrix() {
        Frame frame = new Frame();
        Dialog dlgOwner = new Dialog(frame);
        Window winOwner = new Window(frame); // plain Window: NOT a legal dialog owner
        Dialog d;
        try {
            // Frame-owner family
            d = new Dialog(frame);
            check("".equals(d.getTitle()) && !d.isModal(), "Dialog(Frame)");
            d.dispose();
            d = new Dialog(frame, true);
            check(d.isModal() && d.getModalityType() == Dialog.ModalityType.APPLICATION_MODAL,
                  "Dialog(Frame,boolean=true)");
            d.dispose();
            d = new Dialog(frame, false);
            check(!d.isModal(), "Dialog(Frame,boolean=false)");
            d.dispose();
            d = new Dialog(frame, "ft");
            check("ft".equals(d.getTitle()), "Dialog(Frame,String)");
            d.dispose();
            d = new Dialog(frame, "ft", true);
            check(d.isModal() && "ft".equals(d.getTitle()), "Dialog(Frame,String,boolean)");
            d.dispose();
            d = new Dialog(frame, "ft", false, null);
            check(!d.isModal(), "Dialog(Frame,String,boolean,gc)");
            d.dispose();

            // Dialog-owner family
            d = new Dialog(dlgOwner);
            check(d.getOwner() == dlgOwner, "Dialog(Dialog) owner");
            d.dispose();
            d = new Dialog(dlgOwner, "dt");
            check("dt".equals(d.getTitle()), "Dialog(Dialog,String)");
            d.dispose();
            d = new Dialog(dlgOwner, "dt", true);
            check(d.isModal(), "Dialog(Dialog,String,boolean)");
            d.dispose();
            d = new Dialog(dlgOwner, "dt", false, null);
            check(!d.isModal(), "Dialog(Dialog,String,boolean,gc)");
            d.dispose();

            // Window-typed family: Frame and Dialog owners are legal (accepted as Window)
            Window frameAsWindow = frame;
            d = new Dialog(frameAsWindow);
            check(d.getOwner() == frame && !d.isModal(), "Dialog(Window=Frame)");
            d.dispose();
            d = new Dialog(frameAsWindow, "wt");
            check("wt".equals(d.getTitle()), "Dialog(Window,String)");
            d.dispose();
            d = new Dialog(frameAsWindow, Dialog.ModalityType.TOOLKIT_MODAL);
            check(d.getModalityType() == Dialog.ModalityType.TOOLKIT_MODAL, "Dialog(Window,ModalityType)");
            d.dispose();
            d = new Dialog(frameAsWindow, "wt", Dialog.ModalityType.DOCUMENT_MODAL);
            check(d.isModal() && d.getModalityType() == Dialog.ModalityType.DOCUMENT_MODAL,
                  "Dialog(Window,String,ModalityType)");
            d.dispose();
            d = new Dialog(frameAsWindow, "wt", Dialog.ModalityType.MODELESS, null);
            check(!d.isModal(), "Dialog(Window,String,ModalityType,gc)");
            d.dispose();

            // documented: a plain Window owner is rejected with IllegalArgumentException
            checkThrows(IllegalArgumentException.class, () -> new Dialog(winOwner),
                        "Dialog(plain Window)");
            checkThrows(IllegalArgumentException.class, () -> new Dialog(winOwner, "x"),
                        "Dialog(plain Window,String)");
            checkThrows(IllegalArgumentException.class,
                        () -> new Dialog(winOwner, "x", Dialog.ModalityType.MODELESS),
                        "Dialog(plain Window,String,ModalityType)");

            // null owners are permitted (ownerless dialog)
            d = new Dialog((Frame) null, "nf", true);
            check(d.getOwner() == null && d.isModal(), "Dialog((Frame)null,String,boolean)");
            d.dispose();
            d = new Dialog((Dialog) null);
            check(d.getOwner() == null, "Dialog((Dialog)null)");
            d.dispose();
            d = new Dialog((Window) null, "nw");
            check(d.getOwner() == null, "Dialog((Window)null,String)");
            d.dispose();

            // null ModalityType in constructor behaves like MODELESS default
            d = new Dialog(frameAsWindow, "nm", (Dialog.ModalityType) null);
            check(d.getModalityType() == Dialog.ModalityType.MODELESS, "ctor null modality -> MODELESS");
            d.dispose();

            // default title is the empty string; setTitle(null) stores null (unlike Frame)
            d = new Dialog(frame);
            check("".equals(d.getTitle()), "default title empty");
            d.setTitle(null);
            check(d.getTitle() == null, "setTitle(null) keeps null");
            d.setTitle("x");
            d.setTitle(null);
            check(d.getTitle() == null, "setTitle(null) after value");
        } finally {
            frame.dispose();
            dlgOwner.dispose();
            winOwner.dispose();
        }
    }

    // ---- Part 4: modality type matrix + enum contracts ----
    static void testModalityMatrix() {
        Frame owner = new Frame();
        Dialog d = new Dialog(owner);
        try {
            Dialog.ModalityType[] types = {
                Dialog.ModalityType.MODELESS,
                Dialog.ModalityType.DOCUMENT_MODAL,
                Dialog.ModalityType.APPLICATION_MODAL,
                Dialog.ModalityType.TOOLKIT_MODAL,
            };
            boolean[] modal = {false, true, true, true};
            for (int i = 0; i < types.length; i++) {
                d.setModalityType(types[i]);
                check(d.getModalityType() == types[i], "setModalityType " + types[i]);
                check(d.isModal() == modal[i], "isModal for " + types[i]);
            }
            // documented: null resets to MODELESS (this dialog's default)
            d.setModalityType(null);
            check(d.getModalityType() == Dialog.ModalityType.MODELESS, "setModalityType(null) -> MODELESS");
            check(!d.isModal(), "null modality not modal");

            // enum contracts
            check(Dialog.ModalityType.values().length == 4, "ModalityType has 4 constants");
            check(Dialog.ModalityType.MODELESS.ordinal() == 0, "MODELESS ordinal 0");
            check(Dialog.ModalityType.DOCUMENT_MODAL.ordinal() == 1, "DOCUMENT_MODAL ordinal 1");
            check(Dialog.ModalityType.APPLICATION_MODAL.ordinal() == 2, "APPLICATION_MODAL ordinal 2");
            check(Dialog.ModalityType.TOOLKIT_MODAL.ordinal() == 3, "TOOLKIT_MODAL ordinal 3");
            check(Dialog.ModalityType.valueOf("DOCUMENT_MODAL") == Dialog.ModalityType.DOCUMENT_MODAL,
                  "valueOf DOCUMENT_MODAL");
            checkThrows(IllegalArgumentException.class,
                        () -> Dialog.ModalityType.valueOf("BOGUS_MODAL"),
                        "ModalityType.valueOf(bogus)");

            // modal exclusion enum
            check(Dialog.ModalExclusionType.values().length == 3, "ModalExclusionType 3 constants");
            check(Dialog.ModalExclusionType.NO_EXCLUDE.ordinal() == 0, "NO_EXCLUDE ordinal 0");
            check(Dialog.ModalExclusionType.APPLICATION_EXCLUDE.ordinal() == 1, "APPLICATION_EXCLUDE ordinal 1");
            check(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE.ordinal() == 2, "TOOLKIT_EXCLUDE ordinal 2");
        } finally {
            d.dispose();
            owner.dispose();
        }
    }

    // ---- Part 5: misc contracts ----
    static void testMiscContracts() {
        Frame owner = new Frame();
        try {
            Dialog d = new Dialog(owner, "invisible-title");
            // an invisible dialog's toString carries title and modality
            String s = d.toString();
            check(s.contains("invisible-title"), "toString contains title");
            check(s.contains("MODELESS"), "toString contains modality");
            check(!d.isShowing() && !d.isDisplayable(), "invisible dialog not showing/displayable");
            check(!d.isVisible(), "invisible by default");

            // documented: setUndecorated on displayable dialog throws
            d.setVisible(true);
            checkThrows(IllegalComponentStateException.class, () -> d.setUndecorated(true),
                        "setUndecorated on displayable dialog");
            d.dispose();

            // modality of a never-shown dialog cycles freely
            Dialog d2 = new Dialog(owner);
            d2.setModal(true);
            check(d2.isModal(), "setModal on never-shown dialog");
            d2.dispose();
        } finally {
            owner.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorMatrix();
        testModalityMatrix();
        testMiscContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
