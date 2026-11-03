/*
 * @test
 * @summary javax.naming.ldap.SortControl conformance: white-box structure + black-box behavior.
 * @run main SortControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class SortControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.SortControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(BasicControl.class == c.getSuperclass(), "extends BasicControl");
        check("1.2.840.113556.1.4.473".equals(SortControl.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class), "ctor(String,boolean)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == String[].class && x.getParameterTypes()[1] == boolean.class), "ctor(String[],boolean)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == SortKey[].class && x.getParameterTypes()[1] == boolean.class), "ctor(SortKey[],boolean)");
    }

    static void testBlackBox() throws Exception {
        SortControl sc = new SortControl("cn", true);
        check(SortControl.OID.equals(sc.getID()), "getID");
        check(sc.isCritical(), "critical");
        check(sc.getEncodedValue() != null, "encoded not null");
        check(sc.getEncodedValue().length > 0, "encoded non-empty");

        SortControl sc2 = new SortControl(new String[]{"cn", "sn"}, false);
        check(SortControl.OID.equals(sc2.getID()), "array getID");
        check(!sc2.isCritical(), "array non-critical");
        check(sc2.getEncodedValue() != null, "array encoded");

        SortControl sc3 = new SortControl(new SortKey[]{new SortKey("cn", true, "caseIgnoreMatch")}, true);
        check(sc3.isCritical(), "sortkey critical");
        check(sc3.getEncodedValue() != null, "sortkey encoded");

        check(sc instanceof BasicControl, "instanceof BasicControl");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
