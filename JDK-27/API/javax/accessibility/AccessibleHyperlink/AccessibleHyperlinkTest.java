/*
 * @test
 * @summary javax.accessibility.AccessibleHyperlink conformance: white-box structure + black-box behavior.
 * @run main AccessibleHyperlinkTest
 */

import java.lang.reflect.*;

public class AccessibleHyperlinkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleHyperlink");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasMethod(c, "isValid"), "declares isValid");
        check(hasMethod(c, "getAccessibleActionCount"), "declares getAccessibleActionCount");
        check(hasMethod(c, "doAccessibleAction"), "declares doAccessibleAction");
        check(hasMethod(c, "getAccessibleActionDescription"), "declares getAccessibleActionDescription");
        check(hasMethod(c, "getAccessibleActionObject"), "declares getAccessibleActionObject");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleHyperlink link = new javax.accessibility.AccessibleHyperlink() {
            public boolean isValid() { return true; }
            public int getAccessibleActionCount() { return 2; }
            public boolean doAccessibleAction(int i) { return i == 0; }
            public String getAccessibleActionDescription(int i) { return i == 0 ? "open" : "copy"; }
            public Object getAccessibleActionObject(int i) { return "obj" + i; }
            public Object getAccessibleActionAnchor(int i) { return "anchor" + i; }
            public int getStartIndex() { return 10; }
            public int getEndIndex() { return 20; }
        };
        check(link.isValid(), "isValid");
        check(link.getAccessibleActionCount() == 2, "action count");
        check(link.doAccessibleAction(0), "doAction(0) succeeds");
        check(!link.doAccessibleAction(1), "doAction(1) fails");
        check("open".equals(link.getAccessibleActionDescription(0)), "description[0]");
        check("copy".equals(link.getAccessibleActionDescription(1)), "description[1]");
        check("obj1".equals(link.getAccessibleActionObject(1)), "action object");
        check("anchor0".equals(link.getAccessibleActionAnchor(0)), "action anchor");
        check(link.getStartIndex() == 10, "start index");
        check(link.getEndIndex() == 20, "end index");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
