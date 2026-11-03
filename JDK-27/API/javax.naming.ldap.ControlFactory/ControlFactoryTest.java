/*
 * @test
 * @summary javax.naming.ldap.ControlFactory conformance: white-box structure + black-box behavior.
 * @run main ControlFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;

public class ControlFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.ControlFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "getControlInstance"), "getControlInstance");
    }

    static void testBlackBox() throws Exception {
        final Control produced = new BasicControl("9.9.9", true, new byte[]{1});
        ControlFactory factory = new ControlFactory() {
            public Control getControlInstance(Control ctl) {
                return ctl == null ? produced : ctl;
            }
        };
        check(factory.getControlInstance(null) == produced, "abstract method callable");

        // static lookup with an empty environment returns the control unchanged
        Control input = new BasicControl("1.2.3");
        Control out = ControlFactory.getControlInstance(input, null, new Hashtable<String, Object>());
        check(out == input, "static returns control unchanged without factories");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
