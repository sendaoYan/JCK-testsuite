/*
 * @test
 * @summary javax.naming.ldap.HasControls conformance: white-box structure + black-box behavior.
 * @run main HasControlsTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class HasControlsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.HasControls");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getControls"), "getControls");
    }

    static void testBlackBox() throws Exception {
        HasControls hc = new HasControls() {
            public Control[] getControls() { return new Control[]{new BasicControl("1.2.3")}; }
        };
        Control[] controls = hc.getControls();
        check(controls != null, "getControls not null");
        check(controls.length == 1, "getControls length");
        check("1.2.3".equals(controls[0].getID()), "getControls content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
