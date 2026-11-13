/*
 * @test
 * @summary javax.accessibility.AccessibleAction conformance: white-box structure + black-box behavior.
 * @run main AccessibleActionTest
 */

import java.lang.reflect.*;

public class AccessibleActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleAction");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleActionCount"), "declares getAccessibleActionCount");
        check(hasMethod(c, "getAccessibleActionDescription"), "declares getAccessibleActionDescription");
        check(hasMethod(c, "doAccessibleAction"), "declares doAccessibleAction");
    }

    static void testBlackBox() throws Exception {
        check("toggleexpand".equals(javax.accessibility.AccessibleAction.TOGGLE_EXPAND), "TOGGLE_EXPAND");
        check("increment".equals(javax.accessibility.AccessibleAction.INCREMENT), "INCREMENT");
        check("decrement".equals(javax.accessibility.AccessibleAction.DECREMENT), "DECREMENT");
        check("click".equals(javax.accessibility.AccessibleAction.CLICK), "CLICK");
        check("toggle popup".equals(javax.accessibility.AccessibleAction.TOGGLE_POPUP), "TOGGLE_POPUP");

        javax.accessibility.AccessibleAction act = new javax.accessibility.AccessibleAction() {
            public int getAccessibleActionCount() { return 2; }
            public String getAccessibleActionDescription(int i) { return i == 0 ? "first" : "second"; }
            public boolean doAccessibleAction(int i) { return i == 0; }
        };
        check(act.getAccessibleActionCount() == 2, "action count");
        check("first".equals(act.getAccessibleActionDescription(0)), "description[0]");
        check("second".equals(act.getAccessibleActionDescription(1)), "description[1]");
        check(act.doAccessibleAction(0), "doAction(0) succeeds");
        check(!act.doAccessibleAction(1), "doAction(1) fails");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constant table + recording + real JButton action ----
    static void testContractDeep() throws Exception {
        check("click".equals(javax.accessibility.AccessibleAction.CLICK), "click");
        check("toggleexpand".equals(javax.accessibility.AccessibleAction.TOGGLE_EXPAND), "toggleexpand (no space)");
        check("increment".equals(javax.accessibility.AccessibleAction.INCREMENT), "increment");
        check("decrement".equals(javax.accessibility.AccessibleAction.DECREMENT), "decrement");
        check("toggle popup".equals(javax.accessibility.AccessibleAction.TOGGLE_POPUP), "toggle popup (with space)");
        check("click".equals(javax.accessibility.AccessibleAction.CLICK.toString()), "toString is value");
        javax.accessibility.AccessibleAction a = new javax.accessibility.AccessibleAction() {
            public int getAccessibleActionCount() { return 1; }
            public String getAccessibleActionDescription(int i) { return javax.accessibility.AccessibleAction.CLICK; }
            public boolean doAccessibleAction(int i) { return true; }
        };
        check(a.getAccessibleActionCount() == 1, "count");
        check(a.doAccessibleAction(0), "do");
        check(javax.accessibility.AccessibleAction.class.isInterface(), "interface");
        javax.swing.JButton b = new javax.swing.JButton("Press");
        javax.accessibility.AccessibleAction ba = b.getAccessibleContext().getAccessibleAction();
        check(ba != null && ba.getAccessibleActionCount() == 1, "button one action");
        // AbstractButton.AccessibleAbstractButton 的 click 描述取自
        // UIManager.getString("AbstractButton.clickText")，会随默认 locale 本地化
        // （中文 Windows 上不是 "click"）。用同一个资源键取期望值，保持契约不变。
        String clickText = javax.swing.UIManager.getString("AbstractButton.clickText");
        check(clickText != null && !clickText.isEmpty(), "AbstractButton.clickText resource present");
        check(clickText.equals(ba.getAccessibleActionDescription(0)),
              "button action description is the localised clickText");
        javax.swing.JLabel l = new javax.swing.JLabel();
        check(l.getAccessibleContext().getAccessibleAction() == null, "label no action");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}
