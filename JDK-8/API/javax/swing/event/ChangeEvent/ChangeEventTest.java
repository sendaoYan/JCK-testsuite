/*
 * @test
 * @summary javax.swing.event.ChangeEvent conformance: white-box structure + black-box behavior.
 * @run main ChangeEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.ChangeEvent;

public class ChangeEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.ChangeEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();
        javax.swing.event.ChangeEvent ev = new javax.swing.event.ChangeEvent(source);
        check(ev.getSource() == source, "getSource round-trip");

        // EventObject.toString() should embed the source's own toString().
        String srcString = "my-change-source";
        javax.swing.event.ChangeEvent ev2 = new javax.swing.event.ChangeEvent(srcString);
        check(ev2.getSource() == srcString, "string source identity");
        check(ev2.getSource().equals(srcString), "string source value");
        check(ev2.toString().contains(srcString), "toString embeds source");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        ChangeEvent e = new ChangeEvent("src");
        check("src".equals(e.getSource()), "source");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
