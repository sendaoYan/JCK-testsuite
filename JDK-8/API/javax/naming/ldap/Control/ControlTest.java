/*
 * @test
 * @summary javax.naming.ldap.Control conformance: interface hierarchy and members.
 * @run main ControlTest
 */



import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.Control");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEncodedValue"), "has method getEncodedValue");
        check(names.contains("getID"), "has method getID");
        check(names.contains("isCritical"), "has method isCritical");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCtrlSurface();
        dCtrlCustomImplementation();
        System.out.println("deep: Control contracts OK");
    }

    static class DeepControl implements Control {
        final String id; final boolean crit; final byte[] val;
        DeepControl(String id, boolean crit, byte[] val) { this.id = id; this.crit = crit; this.val = val; }
        public String getID() { return id; }
        public boolean isCritical() { return crit; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dCtrlSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : Control.class.getMethods()) names.add(m.getName());
        check(names.contains("getID"), "declares getID");
        check(names.contains("isCritical"), "declares isCritical");
        check(names.contains("getEncodedValue"), "declares getEncodedValue");
        check(Control.class.getMethods().length == 3, "exactly three methods");
    }

    static void dCtrlCustomImplementation() {
        byte[] ber = {0x30, 0x00};
        DeepControl c = new DeepControl("1.2.3.4", true, ber);
        check("1.2.3.4".equals(c.getID()), "getID");
        check(c.isCritical(), "isCritical");
        check(c.getEncodedValue() == ber, "getEncodedValue");
        // BER encoding is abstract at this level: null is a legal answer
        DeepControl nullVal = new DeepControl("x", false, null);
        check(nullVal.getEncodedValue() == null, "null encoded value legal");
        // a control participates in arrays as any other
        Control[] arr = {c, nullVal, new BasicControl("y")};
        check(arr[2].getID().equals("y") && !arr[2].isCritical(), "interop with BasicControl");
    }

}
