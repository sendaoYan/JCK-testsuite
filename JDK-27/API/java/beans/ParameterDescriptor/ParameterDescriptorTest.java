/*
 * @test
 * @summary java.beans.ParameterDescriptor conformance: white-box structure + black-box behavior.
 *          Public surface is the default constructor only; full inherited FeatureDescriptor
 *          behavior (default state, fall-through rules, flags, values incl. NPE matrix,
 *          attributeNames, toString, identity equals, not serializable); used inside
 *          MethodDescriptor.
 * @run main ParameterDescriptorTest
 */

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ParameterDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ParameterDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
        // public API surface: exactly one public constructor (the default one)
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "exactly one public constructor");
        check(ctors[0].getParameterTypes().length == 0, "constructor is the no-arg default");
        // declares no public methods of its own: everything is inherited from FeatureDescriptor
        int ownPublic = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) ownPublic++;
        }
        check(ownPublic == 0, "no own public methods, got " + ownPublic);
        check(c.getDeclaredFields().length == 0, "no declared fields (stateless subclass)");
    }

    static void testBlackBox() throws Exception {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("p");
        check("p".equals(pd.getName()), "name via FeatureDescriptor");
        pd.setShortDescription("param desc");
        check("param desc".equals(pd.getShortDescription()), "shortDescription");
    }

    static void testDefaultState() {
        ParameterDescriptor pd = new ParameterDescriptor();
        check(pd.getName() == null, "fresh name null");
        check(pd.getDisplayName() == null, "fresh displayName null");
        check(pd.getShortDescription() == null, "fresh shortDescription null");
        check(!pd.isExpert() && !pd.isHidden() && !pd.isPreferred(), "fresh flags false");
        Enumeration<String> en = pd.attributeNames();
        check(en != null && !en.hasMoreElements(), "fresh attributeNames empty");
    }

    static void testFallThroughRules() {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("index");
        check("index".equals(pd.getDisplayName()), "displayName falls back to name");
        check("index".equals(pd.getShortDescription()), "shortDescription falls back to name");
        pd.setDisplayName("Idx");
        check("Idx".equals(pd.getShortDescription()), "shortDescription falls back to displayName");
        pd.setShortDescription("The index");
        check("The index".equals(pd.getShortDescription()), "explicit shortDescription wins");
        check("Idx".equals(pd.getDisplayName()), "displayName unaffected");
    }

    static void testFlags() {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setExpert(true); check(pd.isExpert(), "expert toggle");
        pd.setHidden(true); check(pd.isHidden(), "hidden toggle");
        pd.setPreferred(true); check(pd.isPreferred(), "preferred toggle");
        pd.setExpert(false); check(!pd.isExpert(), "expert off");
    }

    static void testValues() {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setValue("displayNameKey", "v");
        check("v".equals(pd.getValue("displayNameKey")), "value round-trip");
        pd.setValue("n", Integer.valueOf(7));
        check(Integer.valueOf(7).equals(pd.getValue("n")), "non-string value");
        pd.setValue("displayNameKey", "v2");
        check("v2".equals(pd.getValue("displayNameKey")), "overwrite");
        check(pd.getValue("missing") == null, "missing key null");
    }

    static void testValueNegatives() {
        final ParameterDescriptor pd = new ParameterDescriptor();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { pd.setValue(null, "v"); }
        }, "setValue(null key) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { pd.setValue("k", null); }
        }, "setValue(null value) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { pd.getValue(null); }
        }, "getValue(null) -> NPE");
    }

    static void testAttributeNames() {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setValue("a", "1");
        pd.setValue("b", "2");
        Set<String> keys = new HashSet<String>();
        int count = 0;
        for (Enumeration<String> en = pd.attributeNames(); en.hasMoreElements();) {
            keys.add(en.nextElement()); count++;
        }
        check(count == 2, "attributeNames exactly 2 keys, got " + count);
        check(keys.contains("a") && keys.contains("b"), "attributeNames keys match");
    }

    static void testInsideMethodDescriptor() throws Exception {
        ParameterDescriptor p0 = new ParameterDescriptor();
        p0.setName("first");
        ParameterDescriptor p1 = new ParameterDescriptor();
        p1.setName("second");
        java.lang.reflect.Method m = String.class.getMethod("substring", int.class, int.class);
        MethodDescriptor md = new MethodDescriptor(m, new ParameterDescriptor[]{ p0, p1 });
        ParameterDescriptor[] arr = md.getParameterDescriptors();
        check(arr != null && arr.length == 2, "embedded array length");
        check(arr[0] == p0 && arr[1] == p1, "embedded element identity");
        check("first".equals(arr[0].getName()), "embedded name visible");
    }

    static void testToStringAndEquals() {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("x");
        check(pd.toString() != null, "toString non-null");
        ParameterDescriptor other = new ParameterDescriptor();
        other.setName("x");
        check(pd.equals(pd), "equals self");
        check(!pd.equals(other), "identity equals semantics");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(ParameterDescriptor.class),
                "ParameterDescriptor is not Serializable");
        final ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        checkThrows(NotSerializableException.class, new ThrowingRunnable() {
            public void run() throws Exception { oos.writeObject(new ParameterDescriptor()); }
        }, "writeObject(ParameterDescriptor) -> NotSerializableException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDefaultState();
        testFallThroughRules();
        testFlags();
        testValues();
        testValueNegatives();
        testAttributeNames();
        testInsideMethodDescriptor();
        testToStringAndEquals();
        testNotSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
