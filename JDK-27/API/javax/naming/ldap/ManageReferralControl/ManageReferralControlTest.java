/*
 * @test
 * @summary javax.naming.ldap.ManageReferralControl conformance: white-box structure + black-box behavior.
 * @run main ManageReferralControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class ManageReferralControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.ManageReferralControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(BasicControl.class == c.getSuperclass(), "extends BasicControl");
        check("2.16.840.1.113730.3.4.2".equals(ManageReferralControl.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == boolean.class), "ctor(boolean)");
    }

    static void testBlackBox() {
        ManageReferralControl c = new ManageReferralControl();
        check(ManageReferralControl.OID.equals(c.getID()), "getID");
        check(c.isCritical(), "default critical");
        check(c instanceof BasicControl, "instanceof BasicControl");

        ManageReferralControl nc = new ManageReferralControl(false);
        check(!nc.isCritical(), "non-critical ctor");
        check(ManageReferralControl.OID.equals(nc.getID()), "non-critical oid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
