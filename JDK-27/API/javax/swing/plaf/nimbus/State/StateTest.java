/*
 * @test
 * @summary javax.swing.plaf.nimbus.State conformance: white-box structure + black-box behavior.
 * @run main StateTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.plaf.nimbus.State;

public class StateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.State");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        State<JComponent> s1 = new State<JComponent>("MyState") {
            protected boolean isInState(JComponent c) { return true; }
        };
        check("MyState".equals(s1.toString()), "state toString == name");

        State<JComponent> s2 = new State<JComponent>("OtherState") {
            protected boolean isInState(JComponent c) { return false; }
        };
        check("OtherState".equals(s2.toString()), "other state toString");
        check(!s1.toString().equals(s2.toString()), "distinct names distinct toString");

        State<JComponent> s3 = new State<JComponent>("MyState") {
            protected boolean isInState(JComponent c) { return true; }
        };
        check(s1.toString().equals(s3.toString()), "equal names equal toString");
        check(s1 != s3, "equal names distinct instances");
    }
}
