/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: full contract
 *          coverage. type/readable/writable/is accessors, isIs validation
 *          (IllegalArgumentException), clone/equals/hashCode, descriptor ctor,
 *          and toString.
 * @run main MBeanAttributeInfoTest
 */

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;

public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "count", true, true, false);
        check("Count".equals(a.getName()), "name");
        check("int".equals(a.getType()), "type");
        check(a.isReadable() && a.isWritable() && !a.isIs(), "flags");
        MBeanAttributeInfo c = (MBeanAttributeInfo) a.clone();
        check(a.equals(c), "clone equals");
    }

    static void testIsIsValidation() {
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "boolean", "d", false, true, true); }
                }, "isIs non-readable");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "int", "d", true, false, true); }
                }, "isIs non-boolean");
        MBeanAttributeInfo ok = new MBeanAttributeInfo("b", "java.lang.Boolean", "d", true, true, true);
        check(ok.isIs() && ok.isReadable() && ok.isWritable(), "isIs Boolean valid");
    }

    static void testEqualsHashCodeDescriptor() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        MBeanAttributeInfo b = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(!a.equals(new MBeanAttributeInfo("Count", "long", "desc", true, false, false)), "different type");
        Descriptor d = new ImmutableDescriptor(new String[]{"units"}, new Object[]{"ms"});
        MBeanAttributeInfo withD = new MBeanAttributeInfo("Count", "int", "desc", true, false, false, d);
        check("ms".equals(withD.getDescriptor().getFieldValue("units")), "descriptor ctor");
        check(withD.toString() != null && withD.toString().length() > 0, "toString");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testIsIsValidation();
        testEqualsHashCodeDescriptor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
