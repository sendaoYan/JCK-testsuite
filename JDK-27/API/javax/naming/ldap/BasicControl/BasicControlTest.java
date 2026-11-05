/*
 * @test
 * @summary javax.naming.ldap.BasicControl conformance: white-box structure + black-box behavior.
 * @run main BasicControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import java.util.Arrays;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBcConstructors();
        dBcValueHandling();
        dBcSerialization();
        System.out.println("deep: BasicControl contracts OK");
    }

    static void dBcConstructors() {
        BasicControl one = new BasicControl("1.2.3.4");
        check("1.2.3.4".equals(one.getID()), "id-only ctor keeps id");
        check(!one.isCritical(), "id-only ctor default criticality false");
        check(one.getEncodedValue() == null, "id-only ctor has null value");
        BasicControl two = new BasicControl("1.2.3.4", true, new byte[]{1, 2});
        check(two.isCritical(), "criticality honored");
        check(two.getEncodedValue() != null && two.getEncodedValue().length == 2,
                "value stored");
        // subclass sees protected fields
        class Sub extends BasicControl {
            Sub() {
                super("x.y");
                check("x.y".equals(id), "protected id visible to subclass");
                check(criticality == false, "protected criticality default");
                check(value == null, "protected value default");
            }
        }
        new Sub();
    }

    static void dBcValueHandling() {
        byte[] v = {0x04, 0x01, 0x41};
        BasicControl c = new BasicControl("1.2.3", true, v);
        check(java.util.Arrays.equals(c.getEncodedValue(), v), "value bytes preserved");
        check(c.getEncodedValue() == v, "value array returned by reference (no copy)");
        // null value is legal
        BasicControl n = new BasicControl("1.2.3", true, null);
        check(n.getEncodedValue() == null, "null value stays null");
        // mutating the caller array is visible (documented as caller-owned BER)
        v[2] = 0x42;
        check(c.getEncodedValue()[2] == 0x42, "live reference semantics");
        v[2] = 0x41;
    }

    static void dBcSerialization() throws Exception {
        BasicControl c = new BasicControl("1.2.3.4", true, new byte[]{9, 8});
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(c); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            BasicControl back = (BasicControl) ois.readObject();
            check("1.2.3.4".equals(back.getID()), "round-trip id");
            check(back.isCritical(), "round-trip criticality");
            check(java.util.Arrays.equals(back.getEncodedValue(), new byte[]{9, 8}),
                    "round-trip value");
        }
        // Control interface surface
        check(Control.class.isAssignableFrom(BasicControl.class),
                "BasicControl implements Control");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : Control.class.getMethods()) names.add(m.getName());
        check(names.contains("getID") && names.contains("isCritical")
                && names.contains("getEncodedValue"), "Control surface");
    }

}
