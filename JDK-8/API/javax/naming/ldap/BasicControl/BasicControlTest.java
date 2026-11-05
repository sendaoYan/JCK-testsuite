/*
 * @test
 * @summary javax.naming.ldap.BasicControl conformance: id/critical/value.
 * @run main BasicControlTest
 */



import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
public class BasicControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.BasicControl c = new javax.naming.ldap.BasicControl("1.2.3");
        check("1.2.3".equals(c.getID()), "getID");
        check(!c.isCritical(), "critical false default");
        check(c.getEncodedValue() == null, "encoded value null");
        javax.naming.ldap.BasicControl c2 = new javax.naming.ldap.BasicControl("1.2.3", true, new byte[]{1});
        check(c2.isCritical(), "critical flag");
        check(c2.getEncodedValue().length == 1, "getEncodedValue");        testDeepContracts();
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
