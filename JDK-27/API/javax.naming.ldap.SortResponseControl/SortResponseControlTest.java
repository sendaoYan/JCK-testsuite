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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
