/*
 * @test
 * @summary javax.naming.ldap.ManageReferralControl conformance: white-box structure + black-box behavior.
 * @run main ManageReferralControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ManageReferralControl;
import java.util.Arrays;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static String hex(byte[] b) {
        if (b == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static void testDeepContracts() throws Exception {
        check("2.16.840.1.113730.3.4.2".equals(ManageReferralControl.OID),
                "OID constant value");
        ManageReferralControl dflt = new ManageReferralControl();
        check(ManageReferralControl.OID.equals(dflt.getID()), "default ctor uses OID");
        check(dflt.isCritical(), "default criticality true");
        check(dflt.getEncodedValue() == null, "no value payload");
        ManageReferralControl nonCrit = new ManageReferralControl(false);
        check(ManageReferralControl.OID.equals(nonCrit.getID()), "explicit ctor id");
        check(!nonCrit.isCritical(), "explicit criticality false");
        // it is a Control usable in arrays and factory dispatch
        check(dflt instanceof Control, "implements Control");
        check(dflt instanceof BasicControl, "extends BasicControl");
        Control[] arr = {dflt, nonCrit};
        check(arr[0].getID().equals(arr[1].getID()), "same OID both forms");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(dflt); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ManageReferralControl back = (ManageReferralControl) ois.readObject();
            check(back.isCritical(), "round-trip criticality");
            check(ManageReferralControl.OID.equals(back.getID()), "round-trip id");
        }
        System.out.println("deep: ManageReferralControl contracts OK");
    }

}
