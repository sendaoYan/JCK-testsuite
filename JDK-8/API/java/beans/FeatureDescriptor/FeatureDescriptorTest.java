/*
 * @test
 * @summary java.beans.FeatureDescriptor conformance: name/display/short/values.
 *          Default-state contract (name/displayName/shortDescription null, flags false),
 *          displayName/shortDescription fall-through rules, null-key/null-value NPE matrix,
 *          attributeNames exact key set, toString, identity equals, not serializable.
 * @run main FeatureDescriptorTest
 */

import java.beans.FeatureDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class FeatureDescriptorTest {
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
        Class<?> c = Class.forName("java.beans.FeatureDescriptor");
        check(!c.isInterface(), "is a class");
        // exact public surface: all 16 accessors/mutators, nothing else declared here
        Set<String> names = new HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        String[] expected = {"getName", "setName", "getDisplayName", "setDisplayName",
                "isExpert", "setExpert", "isHidden", "setHidden", "isPreferred", "setPreferred",
                "getShortDescription", "setShortDescription", "setValue", "getValue",
                "attributeNames", "toString"};
        check(names.size() == expected.length, "public declared method count, got " + names);
        for (String n : expected) check(names.contains(n), "declares public " + n);
        // no equals/hashCode override at this level (identity semantics)
        boolean hasEquals = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("equals")) hasEquals = true;
        }
        check(!hasEquals, "FeatureDescriptor does not override equals");
        check(FeatureDescriptor.class.getSuperclass() == Object.class, "direct subclass of Object");
    }

    static void testExistingBasics() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("n");
        fd.setDisplayName("display");
        fd.setShortDescription("short");
        check("n".equals(fd.getName()), "name");
        check("display".equals(fd.getDisplayName()), "display name");
        check("short".equals(fd.getShortDescription()), "short description");
        fd.setValue("k", "v");
        check("v".equals(fd.getValue("k")), "value");
    }

    static void testDefaultState() {
        FeatureDescriptor fd = new FeatureDescriptor();
        check(fd.getName() == null, "fresh name null");
        check(fd.getDisplayName() == null, "fresh displayName null");
        check(fd.getShortDescription() == null, "fresh shortDescription null");
        check(!fd.isExpert(), "fresh expert false");
        check(!fd.isHidden(), "fresh hidden false");
        check(!fd.isPreferred(), "fresh preferred false");
        Enumeration<String> en = fd.attributeNames();
        check(en != null, "fresh attributeNames non-null");
        check(!en.hasMoreElements(), "fresh attributeNames empty");
    }

    static void testDisplayNameFallThrough() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("x");
        check("x".equals(fd.getName()), "setName round-trip");
        check("x".equals(fd.getDisplayName()), "displayName falls back to name");
        check("x".equals(fd.getShortDescription()), "shortDescription falls back to displayName/name");
        fd.setDisplayName("D");
        check("D".equals(fd.getDisplayName()), "setDisplayName overrides");
        check("D".equals(fd.getShortDescription()), "shortDescription falls back to displayName");
        fd.setShortDescription("S");
        check("S".equals(fd.getShortDescription()), "setShortDescription overrides");
        check("D".equals(fd.getDisplayName()), "displayName unaffected by shortDescription");
        fd.setShortDescription(null);
        check("D".equals(fd.getShortDescription()), "shortDescription(null) reverts to displayName");
    }

    static void testSetNameNull() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName(null); // allowed, no exception
        check(fd.getName() == null, "setName(null) stores null name");
        check(fd.getDisplayName() == null, "displayName stays null with null name");
    }

    static void testFlagToggles() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setExpert(true); check(fd.isExpert(), "expert on");
        fd.setExpert(false); check(!fd.isExpert(), "expert off");
        fd.setHidden(true); check(fd.isHidden(), "hidden on");
        fd.setHidden(false); check(!fd.isHidden(), "hidden off");
        fd.setPreferred(true); check(fd.isPreferred(), "preferred on");
        fd.setPreferred(false); check(!fd.isPreferred(), "preferred off");
    }

    static void testValues() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue("a", "va");
        check("va".equals(fd.getValue("a")), "string value round-trip");
        fd.setValue("n", Integer.valueOf(42));
        check(Integer.valueOf(42).equals(fd.getValue("n")), "non-string value round-trip");
        fd.setValue("b", Boolean.TRUE);
        check(Boolean.TRUE.equals(fd.getValue("b")), "boolean value round-trip");
        fd.setValue("a", "va2");
        check("va2".equals(fd.getValue("a")), "setValue overwrites");
        check(fd.getValue("unknown") == null, "getValue unknown key null");
        fd.setValue("", "empty");
        check("empty".equals(fd.getValue("")), "empty-string key accepted");
    }

    static void testValueNegatives() {
        final FeatureDescriptor fd = new FeatureDescriptor();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fd.setValue(null, "v"); }
        }, "setValue(null key) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fd.setValue("k", null); }
        }, "setValue(null value) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fd.getValue(null); }
        }, "getValue(null key) -> NPE");
    }

    static void testAttributeNames() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue("a", "1");
        fd.setValue("b", "2");
        fd.setValue("c", "3");
        Set<String> keys = new HashSet<String>();
        Enumeration<String> en = fd.attributeNames();
        int count = 0;
        while (en.hasMoreElements()) { keys.add(en.nextElement()); count++; }
        check(count == 3, "attributeNames yields exactly 3 keys, got " + count);
        check(keys.contains("a") && keys.contains("b") && keys.contains("c"),
                "attributeNames contains exactly the keys set");
        fd.setValue("", "e");
        keys.clear();
        en = fd.attributeNames();
        while (en.hasMoreElements()) keys.add(en.nextElement());
        check(keys.contains(""), "empty-string key appears in attributeNames");
        check(keys.size() == 4, "attributeNames count after 4th key, got " + keys.size());
    }

    static void testToString() {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("nm");
        fd.setValue("b", "2");
        String s = fd.toString();
        check(s != null, "toString non-null");
        check(s.contains("nm"), "toString contains name");
        check(s.contains("b=2"), "toString contains value entry");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(FeatureDescriptor.class),
                "FeatureDescriptor is not Serializable");
        final FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("x");
        final ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        checkThrows(NotSerializableException.class, new ThrowingRunnable() {
            public void run() throws Exception { oos.writeObject(fd); }
        }, "writeObject(FeatureDescriptor) -> NotSerializableException");
    }

    static void testEqualsIdentity() {
        FeatureDescriptor a = new FeatureDescriptor();
        a.setName("same");
        FeatureDescriptor b = new FeatureDescriptor();
        b.setName("same");
        check(a.equals(a), "equals self true");
        check(!a.equals(b), "equals identity semantics for equal-state instance");
        check(a.hashCode() == a.hashCode(), "hashCode stable within instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testExistingBasics();
        testDefaultState();
        testDisplayNameFallThrough();
        testSetNameNull();
        testFlagToggles();
        testValues();
        testValueNegatives();
        testAttributeNames();
        testToString();
        testNotSerializable();
        testEqualsIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
