/*
 * @test
 * @summary javax.swing.event.HyperlinkEvent.EventType conformance: white-box structure + black-box behavior.
 * @run main EventTypeTest
 */

import java.lang.reflect.Modifier;

public class EventTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.HyperlinkEvent$EventType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(Modifier.isFinal(mods) == true, "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        javax.swing.event.HyperlinkEvent.EventType entered = javax.swing.event.HyperlinkEvent.EventType.ENTERED;
        javax.swing.event.HyperlinkEvent.EventType exited = javax.swing.event.HyperlinkEvent.EventType.EXITED;
        javax.swing.event.HyperlinkEvent.EventType activated = javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

        check(entered != null, "ENTERED non-null");
        check(exited != null, "EXITED non-null");
        check(activated != null, "ACTIVATED non-null");
        check(entered.toString().equals("ENTERED"), "ENTERED toString");
        check(exited.toString().equals("EXITED"), "EXITED toString");
        check(activated.toString().equals("ACTIVATED"), "ACTIVATED toString");
        check(entered != exited, "ENTERED != EXITED");
        check(exited != activated, "EXITED != ACTIVATED");
        check(activated != entered, "ACTIVATED != ENTERED");
        check(javax.swing.event.HyperlinkEvent.EventType.ENTERED == entered, "static ENTERED identity stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
