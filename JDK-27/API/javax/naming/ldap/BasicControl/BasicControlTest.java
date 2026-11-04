/*
 * @test
 * @summary javax.naming.ldap.BasicControl conformance: white-box structure + black-box behavior.
 * @run main BasicControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class BasicControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.BasicControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Control.class.isAssignableFrom(c), "implements Control");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class && x.getParameterTypes()[2] == byte[].class), "ctor(String,boolean,byte[])");
        for (String m : new String[]{"getID", "isCritical", "getEncodedValue"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() {
        BasicControl c = new BasicControl("1.2.3.4");
        check("1.2.3.4".equals(c.getID()), "getID");
        check(!c.isCritical(), "default non-critical");
        check(c.getEncodedValue() == null, "default encoded null");

        byte[] val = {1, 2, 3};
        BasicControl c2 = new BasicControl("9.9.9", true, val);
        check("9.9.9".equals(c2.getID()), "getID full");
        check(c2.isCritical(), "critical");
        check(c2.getEncodedValue() != null, "encoded not null");
        check(Arrays.equals(c2.getEncodedValue(), new byte[]{1, 2, 3}), "encoded value");
        check(c2 instanceof Control, "instanceof Control");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
