/*
 * @test
 * @summary javax.management.ImmutableDescriptor conformance: field access, validity, empty descriptor.
 *          Deepened: constructor IAE matrix with exact messages, case-insensitive
 *          name semantics, getFields format ("n=v" / "n=(v)" / "n="), sorted
 *          field names, getFieldValues(null) + null-name entries, immutable-
 *          mutation RuntimeOperationsException(UnsupportedOperationException)
 *          matrix incl. same-value no-op and absent-remove no-op, clone
 *          identity, union semantics (single-input identity, conflict IAE,
 *          null-element skip, null-array NPE), isValid() always true (vs
 *          DescriptorSupport), EMPTY_DESCRIPTOR facts + readResolve,
 *          serialization round-trip keeping immutability, array-value cloning.
 * @run main ImmutableDescriptorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;

public class ImmutableDescriptorTest {
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

    static Object ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    public static void main(String[] args) throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"});
        check("b".equals(d.getFieldValue("a")), "field value");
        check(d.getFieldNames().length == 1 && "a".equals(d.getFieldNames()[0]), "field names");
        check(d.isValid(), "isValid");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR != null, "EMPTY_DESCRIPTOR");
        check(d.equals(new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"})), "equals");
        check(!d.equals(new ImmutableDescriptor(new String[]{"a"}, new Object[]{"c"})), "not equals");
        testConstructorExceptionMatrix();
        testCaseInsensitiveSemantics();
        testGetFieldsFormat();
        testFieldValuesMatrix();
        testImmutabilityMatrix();
        testUnionSemantics();
        testIsValidAlwaysTrue();
        testEmptyDescriptor();
        testSerialization();
        testArrayValueCloning();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constructor exception matrix (IllegalArgumentException, not ROE) ----

    static void expectIAE(final String[] names, final Object[] values, String message) {
        try {
            new ImmutableDescriptor(names, values);
            throw new AssertionError("expected IAE: " + message);
        } catch (IllegalArgumentException ex) {
            check(message.equals(ex.getMessage()),
                    "ctor IAE message '" + ex.getMessage() + "' for " + message);
        }
    }

    static void testConstructorExceptionMatrix() {
        expectIAE(new String[]{"a", "b"}, new Object[]{"x"}, "Different size arrays");
        expectIAE(new String[]{"a"}, new Object[]{"x", "y"}, "Different size arrays");
        expectIAE(new String[]{null}, new Object[]{"x"}, "Empty or null field name");
        expectIAE(new String[]{""}, new Object[]{"x"}, "Empty or null field name");
        expectIAE(new String[]{"a", "a"}, new Object[]{"x", "y"}, "Duplicate field name: a");
        // duplicates are detected case-insensitively
        expectIAE(new String[]{"a", "A"}, new Object[]{"x", "y"}, "Duplicate field name: A");
        try {
            new ImmutableDescriptor((String[]) null);
            check(false, "null names array should throw");
        } catch (IllegalArgumentException ex) {
            check("Null fields parameter".equals(ex.getMessage()), "null varargs IAE message");
        }
        try {
            new ImmutableDescriptor(new String[]{"a"}, null);
            check(false, "null values array should throw");
        } catch (IllegalArgumentException ex) {
            check("Null array parameter".equals(ex.getMessage()), "null values IAE message");
        }
        try {
            new ImmutableDescriptor((Map<String, ?>) null);
            check(false, "null Map should throw");
        } catch (IllegalArgumentException ex) {
            check("Null Map".equals(ex.getMessage()), "null Map IAE message");
        }
        // String... form: '=' is required, first '=' splits (a=b=c -> a -> b=c)
        try {
            new ImmutableDescriptor("noequals");
            check(false, "missing '=' should throw");
        } catch (IllegalArgumentException ex) {
            check("Missing = character: noequals".equals(ex.getMessage()), "missing '=' IAE message");
        }
        try {
            new ImmutableDescriptor("=v");
            check(false, "empty name via string form should throw");
        } catch (IllegalArgumentException ex) {
            check("Empty or null field name".equals(ex.getMessage()), "empty name IAE message");
        }
        check("b=c".equals(new ImmutableDescriptor("a=b=c").getFieldValue("a")),
                "first '=' splits the pair (value keeps later '=' chars)");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("k", "v");
        check("v".equals(new ImmutableDescriptor(m).getFieldValue("k")), "Map ctor");
        // exactly three public constructors: no (String,Object) single-field convenience form
        check(ImmutableDescriptor.class.getConstructors().length == 3,
                "exactly 3 public constructors (no single-field convenience ctor)");
    }

    // ---- deepened: case-insensitive field-name semantics --------------------

    static void testCaseInsensitiveSemantics() {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"Name"}, new Object[]{"v"});
        check("v".equals(d.getFieldValue("name")), "getFieldValue is case-insensitive");
        check("v".equals(d.getFieldValue("NAME")), "getFieldValue upper-case lookup");
        check(d.getFieldNames()[0].equals("Name"), "original case preserved in getFieldNames");
        ImmutableDescriptor lower = new ImmutableDescriptor(new String[]{"name"}, new Object[]{"v"});
        check(d.equals(lower), "equals ignores field-name case");
        check(d.hashCode() == lower.hashCode(), "hashCode ignores field-name case");
        // cross-implementation equality with the mutable DescriptorSupport
        DescriptorSupport ds = new DescriptorSupport(new String[]{"NAME"}, new Object[]{"v"});
        check(d.equals(ds), "ImmutableDescriptor.equals(DescriptorSupport) true");
        check(ds.equals(d), "DescriptorSupport.equals(ImmutableDescriptor) true");
        check(!d.equals(null) && !d.equals("name=v"), "equals null/non-Descriptor false");
    }

    // ---- deepened: getFields "name=value" format rules ----------------------

    static void testGetFieldsFormat() {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"s", "n", "i"},
                new Object[]{"str", null, 5});
        // fields are stored sorted (case-insensitive alphabetical): i, n, s
        String[] f = d.getFields();
        check(f.length == 3 && "i=(5)".equals(f[0]), "non-String value wrapped in parentheses");
        check("n=".equals(f[1]), "null value rendered as empty after '='");
        check("s=str".equals(f[2]), "String value rendered raw");
        // field names are stored in case-insensitive alphabetical order
        String[] names = new ImmutableDescriptor(new String[]{"zz", "AA", "mm"},
                new Object[]{1, 2, 3}).getFieldNames();
        check(names[0].equals("AA") && names[1].equals("mm") && names[2].equals("zz"),
                "field names sorted case-insensitively");
        check(names != new ImmutableDescriptor(new String[]{"zz", "AA", "mm"},
                new Object[]{1, 2, 3}).getFieldNames(), "getFieldNames returns a clone");
        check(d.toString().equals("{i=5, n=null, s=str}"),
                "toString format: {i=5, n=null, s=str}");
    }

    // ---- deepened: getFieldValues matrix ------------------------------------

    static void testFieldValuesMatrix() {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"a", "b"}, new Object[]{"x", 5});
        Object[] all = d.getFieldValues((String[]) null);
        check(all.length == 2 && "x".equals(all[0]) && Integer.valueOf(5).equals(all[1]),
                "getFieldValues(null) returns all values");
        Object[] mixed = d.getFieldValues(null, "");
        check(mixed.length == 2 && mixed[0] == null && mixed[1] == null,
                "null/empty names yield null entries");
        Object[] ordered = d.getFieldValues("b", "a", "absent");
        check(ordered.length == 3 && Integer.valueOf(5).equals(ordered[0])
                && "x".equals(ordered[1]) && ordered[2] == null,
                "getFieldValues follows requested order, absent -> null");
    }

    // ---- deepened: immutability exception matrix -----------------------------

    static void expectROE(Class<? extends Throwable> causeType, String causeMessage,
                          ThrowingRunnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected RuntimeOperationsException");
        } catch (RuntimeOperationsException ex) {
            check(causeType.isInstance(ex.getCause()), msg + " cause type "
                    + (ex.getCause() == null ? "null" : ex.getCause().getClass().getSimpleName()));
            if (causeMessage != null)
                check(causeMessage.equals(ex.getCause().getMessage()),
                        msg + " cause message: " + ex.getCause().getMessage());
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected RuntimeOperationsException but got "
                    + t.getClass().getName(), t);
        }
    }

    static void testImmutabilityMatrix() {
        final ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"a"}, new Object[]{"x"});
        expectROE(UnsupportedOperationException.class, "Descriptor is read-only",
                new ThrowingRunnable() { public void run() { d.setField("b", "y"); } },
                "setField(new field)");
        expectROE(UnsupportedOperationException.class, "Descriptor is read-only",
                new ThrowingRunnable() { public void run() { d.removeField("a"); } },
                "removeField(existing)");
        expectROE(IllegalArgumentException.class, "Null or empty field name",
                new ThrowingRunnable() { public void run() { d.getFieldValue(null); } },
                "getFieldValue(null)");
        expectROE(IllegalArgumentException.class, "Null or empty field name",
                new ThrowingRunnable() { public void run() { d.getFieldValue(""); } },
                "getFieldValue(empty)");
        expectROE(IllegalArgumentException.class, "Null or empty field name",
                new ThrowingRunnable() { public void run() { d.setField(null, "v"); } },
                "setField(null name)");
        expectROE(IllegalArgumentException.class, "Different array sizes",
                new ThrowingRunnable() { public void run() { d.setFields(new String[]{"a"}, new Object[]{}); } },
                "setFields(different sizes)");
        expectROE(IllegalArgumentException.class, "Null argument",
                new ThrowingRunnable() { public void run() { d.setFields(null, null); } },
                "setFields(null arrays)");
        // no-ops that must NOT throw: setting an identical value, removing an absent field
        d.setField("a", "x");
        check(true, "setField with identical value is a no-op");
        d.removeField("absent");
        d.removeField(null);
        check(true, "removeField of absent/null name is a no-op");
        d.setFields(new String[]{"a"}, new Object[]{"x"});
        check(true, "setFields with identical values is a no-op");
        // clone returns the same instance (immutable)
        check(d.clone() == d, "clone() returns same instance");
        check(((Descriptor) d).clone() == d, "Descriptor-typed clone() returns same instance");
    }

    // ---- deepened: union semantics ------------------------------------------

    static void testUnionSemantics() {
        ImmutableDescriptor one = new ImmutableDescriptor("a=1", "b=2");
        check(ImmutableDescriptor.union(one) == one,
                "single non-empty ImmutableDescriptor input returned as-is");
        check(ImmutableDescriptor.union() == ImmutableDescriptor.EMPTY_DESCRIPTOR,
                "no arguments returns EMPTY_DESCRIPTOR");
        check(ImmutableDescriptor.union(null, one, null) == one,
                "null descriptors skipped, single non-empty returned");
        ImmutableDescriptor merged = ImmutableDescriptor.union(
                new ImmutableDescriptor("a=1"), new ImmutableDescriptor("b=2"));
        check("1".equals(merged.getFieldValue("a")) && "2".equals(merged.getFieldValue("b")),
                "union merges disjoint fields");
        check(ImmutableDescriptor.union(new ImmutableDescriptor("a=1"), new ImmutableDescriptor("a=1"))
                .getFieldValue("a").equals("1"), "union consistent duplicates allowed");
        try {
            ImmutableDescriptor.union(new ImmutableDescriptor("a=1"), new ImmutableDescriptor("a=2"));
            check(false, "union conflict should throw IAE");
        } catch (IllegalArgumentException ex) {
            check(ex.getMessage().startsWith("Inconsistent values for descriptor field a:"),
                    "union conflict IAE message: " + ex.getMessage());
        }
        try {
            ImmutableDescriptor.union(new ImmutableDescriptor("a=1"), new ImmutableDescriptor("A=2"));
            check(false, "union case-insensitive conflict should throw IAE");
        } catch (IllegalArgumentException ex) { check(true, "union conflict is case-insensitive"); }
        try {
            ImmutableDescriptor.union((Descriptor[]) null);
            check(false, "union(null array) should throw NPE");
        } catch (NullPointerException ex) { check(true, "union(null array) NPE"); }
        // union with a mutable Descriptor also works
        ImmutableDescriptor withMutable = ImmutableDescriptor.union(
                new ImmutableDescriptor("a=1"), new DescriptorSupport(new String[]{"b"}, new Object[]{"2"}));
        check("2".equals(withMutable.getFieldValue("b")), "union accepts mutable Descriptor input");
    }

    // ---- deepened: isValid() is always true for ImmutableDescriptor ---------

    static void testIsValidAlwaysTrue() {
        // ImmutableDescriptor.isValid() always returns true, even for field names
        // containing '.' or values of arbitrary types - both constructible because
        // the constructor only checks name null/emptiness/duplicates.
        // (DescriptorSupport.isValid() is the strict one; see DescriptorTest.)
        check(new ImmutableDescriptor("a.b=c").isValid(), "'.'-named field still valid");
        check(new ImmutableDescriptor(new String[]{"k"}, new Object[]{new Object()}).isValid(),
                "arbitrary Object value still valid");
        check(new ImmutableDescriptor("a=").isValid(), "null value still valid");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR.isValid(), "empty descriptor valid");
    }

    // ---- deepened: EMPTY_DESCRIPTOR facts ------------------------------------

    static void testEmptyDescriptor() throws Exception {
        ImmutableDescriptor e = ImmutableDescriptor.EMPTY_DESCRIPTOR;
        check(e.getFieldNames().length == 0, "EMPTY has no fields");
        check(e.getFields().length == 0, "EMPTY getFields empty");
        check(e.getFieldValues((String[]) null).length == 0, "EMPTY getFieldValues empty");
        check("{}".equals(e.toString()), "EMPTY toString is {}");
        check(e.clone() == e, "EMPTY clone is itself");
        check(e.isValid(), "EMPTY is valid");
        check(ser(e) == e, "EMPTY serialization uses readResolve and returns same instance");
    }

    // ---- deepened: serialization round-trip ----------------------------------

    static void testSerialization() throws Exception {
        ImmutableDescriptor original = new ImmutableDescriptor(new String[]{"a", "b"}, new Object[]{"x", 5});
        ImmutableDescriptor read = (ImmutableDescriptor) ser(original);
        check(read.getClass() == ImmutableDescriptor.class, "round-trip keeps the class");
        check(read.equals(original) && original.equals(read), "round-trip equals");
        check("x".equals(read.getFieldValue("a")), "round-trip keeps values");
        // JDK-8 quirk (version difference): the transient hashCode cache is not
        // reinitialized on deserialization (default 0 != -1 sentinel), so a
        // deserialized descriptor that equals the original can report a different
        // hashCode. JDK-27 resets the cache in readResolve. Lock the JDK-8 reality.
        check(read.hashCode() == 0 && original.hashCode() != 0,
                "JDK-8: deserialized hashCode is the stale 0 cache (equals/hashCode deviation)");
        try {
            read.setField("c", "z");
            check(false, "deserialized descriptor must stay immutable");
        } catch (RuntimeOperationsException ex) {
            check(ex.getCause() instanceof UnsupportedOperationException,
                    "deserialized setField still ROE(UnsupportedOperationException)");
        }
    }

    // ---- deepened: array field values are handed out as clones ---------------

    static void testArrayValueCloning() throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"arr"},
                new Object[]{new int[]{1, 2}});
        int[] v1 = (int[]) d.getFieldValue("arr");
        int[] v2 = (int[]) d.getFieldValue("arr");
        check(v1 != v2, "array value cloned on each getFieldValue");
        v1[0] = 99;
        check(((int[]) d.getFieldValue("arr"))[0] == 1, "mutating the returned clone is isolated");
    }
}
