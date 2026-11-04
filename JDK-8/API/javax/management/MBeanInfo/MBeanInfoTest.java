/*
 * @test
 * @summary javax.management.MBeanInfo conformance: full contract coverage.
 *          constructors, accessors, null arrays -> empty arrays, clone deep
 *          independence + equals/hashCode, array getters return defensive
 *          copies, and the nested MBeanAttributeInfo isIs validation
 *          (IllegalArgumentException) / MBeanOperationInfo signature defaults.
 * @run main MBeanInfoTest
 */

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class MBeanInfoTest {
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

    static MBeanInfo sample() {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, false, false);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0]);
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO);
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        return new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{ai}, new MBeanConstructorInfo[]{ci},
            new MBeanOperationInfo[]{oi}, new MBeanNotificationInfo[]{ni});
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, false, false);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0]);
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO);
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        MBeanInfo info = new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{ai}, new MBeanConstructorInfo[]{ci},
            new MBeanOperationInfo[]{oi}, new MBeanNotificationInfo[]{ni});
        check("com.example.Foo".equals(info.getClassName()), "class name");
        check("desc".equals(info.getDescription()), "description");
        check(info.getAttributes().length == 1 && "int".equals(info.getAttributes()[0].getType()), "attributes");
        check(info.getOperations().length == 1 && "void".equals(info.getOperations()[0].getReturnType()), "operations");
        check(info.getConstructors().length == 1, "constructors");
        check(info.getNotifications().length == 1 && info.getNotifications()[0].getNotifTypes().length == 1, "notifications");
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(info.equals(copy), "clone equals");
    }

    static void testNullArrays() {
        MBeanInfo info = new MBeanInfo("com.example.Foo", "desc", null, null, null, null);
        check(info.getAttributes().length == 0, "null attributes -> empty");
        check(info.getOperations().length == 0, "null operations -> empty");
        check(info.getConstructors().length == 0, "null constructors -> empty");
        check(info.getNotifications().length == 0, "null notifications -> empty");
    }

    static void testCloneIndependence() {
        MBeanInfo info = sample();
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(info.equals(copy) && copy.equals(info), "clone equals symmetric");
        check(info.hashCode() == copy.hashCode(), "clone hashCode");
        // clone is a deep copy: mutating the copy's array does not affect original
        copy.getAttributes()[0] = null;
        check(info.getAttributes()[0] != null, "clone array independent");
    }

    static void testDefensiveArrayGetters() {
        MBeanInfo info = sample();
        MBeanAttributeInfo[] a1 = info.getAttributes();
        a1[0] = null;
        check(info.getAttributes()[0] != null, "getAttributes returns defensive copy");
    }

    static void testAttributeInfoIsIsValidation() {
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "boolean", "d", false, true, true); }
                }, "isIs with non-readable -> IAE");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "int", "d", true, false, true); }
                }, "isIs with non-boolean -> IAE");
        // valid: isIs with readable boolean
        MBeanAttributeInfo ok = new MBeanAttributeInfo("b", "boolean", "d", true, false, true);
        check(ok.isIs() && ok.isReadable() && !ok.isWritable(), "isIs valid");
    }

    static void testOperationSignatureDefaults() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d", null, "int", MBeanOperationInfo.INFO);
        check(o.getSignature().length == 0, "null signature -> empty");
        check("int".equals(o.getReturnType()), "return type");
        check(o.getImpact() == MBeanOperationInfo.INFO, "impact");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullArrays();
        testCloneIndependence();
        testDefensiveArrayGetters();
        testAttributeInfoIsIsValidation();
        testOperationSignatureDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
