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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
