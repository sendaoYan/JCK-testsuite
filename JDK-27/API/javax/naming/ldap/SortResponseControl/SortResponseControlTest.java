/*
 * @test
 * @summary javax.naming.ldap.SortResponseControl conformance: white-box structure + black-box behavior.
 * @run main SortResponseControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import javax.naming.NamingException;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortResponseControl;
import java.util.Arrays;

public class SortResponseControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.SortResponseControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(BasicControl.class == c.getSuperclass(), "extends BasicControl");
        check("1.2.840.113556.1.4.474".equals(SortResponseControl.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class && x.getParameterTypes()[2] == byte[].class), "ctor(String,boolean,byte[])");
        for (String m : new String[]{"isSorted", "getResultCode", "getAttributeID", "getException"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        // SEQUENCE { ENUMERATED 0 }
        byte[] ok = {0x30, 0x03, 0x0a, 0x01, 0x00};
        SortResponseControl c = new SortResponseControl(SortResponseControl.OID, true, ok);
        check(SortResponseControl.OID.equals(c.getID()), "getID");
        check(c.isCritical(), "critical");
        check(c.isSorted(), "isSorted");
        check(c.getResultCode() == 0, "result code 0");
        check(c.getAttributeID() == null, "attributeID null");
        check(c.getException() == null, "exception null");

        // SEQUENCE { ENUMERATED 16 (noSuchAttribute), [0] "cn" }
        byte[] fail = {0x30, 0x07, 0x0a, 0x01, 0x10, (byte) 0x80, 0x02, 0x63, 0x6e};
        SortResponseControl c2 = new SortResponseControl(SortResponseControl.OID, false, fail);
        check(!c2.isSorted(), "not sorted");
        check(c2.getResultCode() == 16, "result code 16");
        check("cn".equals(c2.getAttributeID()), "bad attributeID");
        check(c2.getException() != null, "exception not null");
        check(c2.getException() instanceof NoSuchAttributeException, "exception type NoSuchAttributeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSrcOid();
        dSrcParsing();
        dSrcExceptions();
        dSrcNegative();
        System.out.println("deep: SortResponseControl contracts OK");
    }

    static void dSrcOid() {
        check("1.2.840.113556.1.4.474".equals(SortResponseControl.OID), "OID constant");
        check(!SortResponseControl.OID.equals(SortControl.OID), "distinct from request OID");
    }

    static void dSrcParsing() throws Exception {
        // SEQUENCE { ENUMERATED 0, [0] "cn" }  (code tag is ENUMERATED 0x0a)
        byte[] ok = {0x30, 0x07, 0x0a, 0x01, 0x00, (byte)0x80, 0x02, 'c', 'n'};
        SortResponseControl c = new SortResponseControl(SortResponseControl.OID, true, ok);
        check(c.isSorted(), "result 0 means sorted");
        check(c.getResultCode() == 0, "result code 0");
        check("cn".equals(c.getAttributeID()), "offending attribute parsed");
        check(c.getException() == null, "no exception for success");
        check(c.isCritical(), "criticality stored");
        // attribute is optional
        byte[] noAttr = {0x30, 0x03, 0x0a, 0x01, 0x00};
        SortResponseControl bare = new SortResponseControl("x", false, noAttr);
        check(bare.isSorted(), "success without attribute");
        check(bare.getAttributeID() == null, "no attribute -> null");
        // non-zero result: 18 = inappropriateMatching
        byte[] bad = {0x30, 0x08, 0x0a, 0x01, 0x12, (byte)0x80, 0x03, 'a', 'b', 'c'};
        SortResponseControl fail = new SortResponseControl("x", true, bad);
        check(!fail.isSorted(), "result 18 means not sorted");
        check(fail.getResultCode() == 18, "result code 18");
        check("abc".equals(fail.getAttributeID()), "attribute parsed on failure");
    }

    static void dSrcExceptions() throws Exception {
        byte[] bad = {0x30, 0x08, 0x0a, 0x01, 0x12, (byte)0x80, 0x03, 'a', 'b', 'c'};
        SortResponseControl fail = new SortResponseControl("x", true, bad);
        NamingException e = fail.getException();
        check(e instanceof javax.naming.directory.InvalidSearchFilterException,
                "code 18 maps to InvalidSearchFilterException");
        check(e.getMessage().contains("error code 18"), "message carries LDAP code");
    }

    static void dSrcNegative() {
        boolean ioe1 = false, ioe2 = false;
        try { new SortResponseControl("x", true, new byte[]{0x30}); }
        catch (java.io.IOException e) { ioe1 = true; }
        try { new SortResponseControl("x", true, new byte[]{0x30, 0x02, 0x02, 0x01}); }
        catch (java.io.IOException e) { ioe2 = true; }
        check(ioe1, "truncated body -> IOException");
        check(ioe2, "wrong tag (INTEGER instead of ENUMERATED) -> IOException");
    }

}
