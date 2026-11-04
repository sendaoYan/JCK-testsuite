/*
 * @test
 * @summary javax.management.ImmutableDescriptor conformance: white-box structure + black-box behavior.
 *          Deepened: constructor IAE matrix with exact messages, getFields format
 *          ("n=v"/"n=(v)"/"n="), sorted+cloned field names, getFieldValues(null),
 *          same-value no-ops, union identity/null-skip/null-array NPE,
 *          isValid() always true, EMPTY_DESCRIPTOR facts + readResolve,
 *          serialization keeps immutability, array-value cloning, 3-ctor shape.
 * @run main ImmutableDescriptorTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;
import javax.management.modelmbean.DescriptorSupport;

public class ImmutableDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ImmutableDescriptor");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Descriptor.class.isAssignableFrom(c), "implements Descriptor");
        check(hasMethod(c, "getFieldValue"), "has getFieldValue");
        check(hasMethod(c, "getFieldValues"), "has getFieldValues");
        check(hasMethod(c, "getFieldNames"), "has getFieldNames");
        check(hasMethod(c, "union"), "has static union");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR != null, "EMPTY_DESCRIPTOR constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"name", "num"}, new Object[]{"value", 1});
        check("value".equals(d.getFieldValue("name")), "getFieldValue(name)");
        check(d.getFieldValue("num").equals(1), "getFieldValue(num)");
        check(d.getFieldValue("absent") == null, "absent field returns null");
        check(d.getFieldNames().length == 2, "two field names");
        check(d.isValid(), "isValid");

        // field name lookup is case-insensitive
        check("value".equals(d.getFieldValue("NAME")), "case-insensitive field lookup");

        // equals: same fields and values (case-insensitive names)
        ImmutableDescriptor d2 = new ImmutableDescriptor(new String[]{"Name", "Num"}, new Object[]{"value", 1});
        check(d.equals(d2), "equals ignores field-name case");
        check(d.hashCode() == d2.hashCode(), "hashCode consistent with equals");
        check(!d.equals(new ImmutableDescriptor(new String[]{"name"}, new Object[]{"value"})), "different field count not equal");
        check(!d.equals(new ImmutableDescriptor(new String[]{"name", "num"}, new Object[]{"value", 2})), "different value not equal");
        check(!d.equals(null) && !d.equals("x"), "not equal null/non-Descriptor");

        // EMPTY_DESCRIPTOR
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR.getFieldNames().length == 0, "EMPTY_DESCRIPTOR empty");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR.isValid(), "EMPTY_DESCRIPTOR valid");

        // union merges fields; descriptors with disjoint fields combine cleanly
        ImmutableDescriptor u = ImmutableDescriptor.union(
                new ImmutableDescriptor(new String[]{"a", "b"}, new Object[]{"1", "2"}),
                new ImmutableDescriptor(new String[]{"c"}, new Object[]{"3"}));
        check(u.getFieldNames().length == 3, "union has 3 fields");
        check("1".equals(u.getFieldValue("a")) && "2".equals(u.getFieldValue("b")) && "3".equals(u.getFieldValue("c")), "union keeps fields");

        // union with consistent duplicate values is allowed
        ImmutableDescriptor u2 = ImmutableDescriptor.union(
                new ImmutableDescriptor(new String[]{"a"}, new Object[]{"1"}),
                new ImmutableDescriptor(new String[]{"a", "b"}, new Object[]{"1", "2"}));
        check(u2.getFieldNames().length == 2 && "1".equals(u2.getFieldValue("a")), "union consistent duplicates");

        // union with conflicting duplicate values throws IllegalArgumentException
        boolean threwUnion = false;
        try {
            ImmutableDescriptor.union(
                    new ImmutableDescriptor(new String[]{"b"}, new Object[]{"2"}),
                    new ImmutableDescriptor(new String[]{"b"}, new Object[]{"9"}));
        } catch (IllegalArgumentException ex) { threwUnion = true; }
        check(threwUnion, "union throws on inconsistent values");

        // varargs constructor maps name=value pairs
        ImmutableDescriptor v = new ImmutableDescriptor("x=1", "y=2");
        check("1".equals(v.getFieldValue("x")) && "2".equals(v.getFieldValue("y")), "varargs constructor");

        // Map constructor
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k", "v");
        ImmutableDescriptor m = new ImmutableDescriptor(map);
        check("v".equals(m.getFieldValue("k")), "Map constructor");

        // getFieldValues with requested names (in order)
        Object[] vals = d.getFieldValues("num", "name", "absent");
        check(vals.length == 3, "getFieldValues length");
        check(vals[0].equals(1) && vals[1].equals("value") && vals[2] == null, "getFieldValues values");

        // immutability: setField / setFields / removeField throw RuntimeOperationsException wrapping UnsupportedOperationException
        boolean threwSet = false;
        try { d.setField("x", "y"); } catch (RuntimeOperationsException ex) { threwSet = (ex.getCause() instanceof UnsupportedOperationException); }
        check(threwSet, "setField throws RuntimeOperationsException(UnsupportedOperationException)");

        boolean threwSetFields = false;
        try { d.setFields(new String[]{"x"}, new Object[]{"y"}); } catch (RuntimeOperationsException ex) { threwSetFields = true; }
        check(threwSetFields, "setFields throws RuntimeOperationsException");

        boolean threwRemove = false;
        try { d.removeField("name"); } catch (RuntimeOperationsException ex) { threwRemove = true; }
        check(threwRemove, "removeField throws RuntimeOperationsException");

        // clone() on an immutable descriptor returns the same instance
        Descriptor clone = d.clone();
        check(clone == d, "clone returns same immutable instance");
        check(clone.equals(d), "clone equals original");

        // toString is non-null
        check(d.toString() != null, "toString non-null");
    }

    // ---- deepened: constructor exception matrix (IAE with exact messages) ----

    static void expectIAE(String[] names, Object[] values, String message) {
        try {
            new ImmutableDescriptor(names, values);
            throw new AssertionError("expected IAE: " + message);
        } catch (IllegalArgumentException ex) {
            if (!message.equals(ex.getMessage()))
                throw new AssertionError("ctor IAE message '" + ex.getMessage() + "' expected '" + message + "'");
            passed++;
        }
    }

    static void testConstructorMatrix() {
        expectIAE(new String[]{"a", "b"}, new Object[]{"x"}, "Different size arrays");
        expectIAE(new String[]{null}, new Object[]{"x"}, "Empty or null field name");
        expectIAE(new String[]{""}, new Object[]{"x"}, "Empty or null field name");
        expectIAE(new String[]{"a", "a"}, new Object[]{"x", "y"}, "Duplicate field name: a");
        expectIAE(new String[]{"a", "A"}, new Object[]{"x", "y"}, "Duplicate field name: A");
        try { new ImmutableDescriptor("noequals"); check(false, "missing '=' should throw"); }
        catch (IllegalArgumentException ex) {
            check("Missing = character: noequals".equals(ex.getMessage()), "missing '=' IAE message"); }
        try { new ImmutableDescriptor("=v"); check(false, "empty name should throw"); }
        catch (IllegalArgumentException ex) {
            check("Empty or null field name".equals(ex.getMessage()), "empty name IAE message"); }
        try { new ImmutableDescriptor((Map<String, ?>) null); check(false, "null Map should throw"); }
        catch (IllegalArgumentException ex) {
            check("Null Map".equals(ex.getMessage()), "null Map IAE message"); }
        check("b=c".equals(new ImmutableDescriptor("a=b=c").getFieldValue("a")),
                "first '=' splits the pair");
        check(ImmutableDescriptor.class.getConstructors().length == 3,
                "exactly 3 public constructors (no single-field convenience ctor)");
    }

    // ---- deepened: getFields format + sorted cloned names + null lookups -----

    static void testGetFieldsFormat() {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"s", "n", "i"},
                new Object[]{"str", null, 5});
        // fields are stored sorted (case-insensitive alphabetical): i, n, s
        String[] f = d.getFields();
        check("i=(5)".equals(f[0]), "non-String value wrapped in parentheses");
        check("n=".equals(f[1]), "null value rendered as empty after '='");
        check("s=str".equals(f[2]), "String value rendered raw");
        String[] names = new ImmutableDescriptor(new String[]{"zz", "AA", "mm"},
                new Object[]{1, 2, 3}).getFieldNames();
        check(names[0].equals("AA") && names[1].equals("mm") && names[2].equals("zz"),
                "field names sorted case-insensitively");
        Object[] all = d.getFieldValues((String[]) null);
        check(all.length == 3 && Integer.valueOf(5).equals(all[0]) && all[1] == null && "str".equals(all[2]),
                "getFieldValues(null) returns all values in sorted field order");
        Object[] mixed = d.getFieldValues(null, "");
        check(mixed[0] == null && mixed[1] == null, "null/empty names yield null entries");
        check("str".equals(d.getFieldValue("S")), "case-insensitive lookup");
        // cross-implementation equality with the mutable DescriptorSupport
        DescriptorSupport ds = new DescriptorSupport(new String[]{"S"}, new Object[]{"str"});
        check(new ImmutableDescriptor(new String[]{"S"}, new Object[]{"str"}).equals(ds),
                "ImmutableDescriptor.equals(DescriptorSupport) true");
        check(ds.equals(new ImmutableDescriptor(new String[]{"s"}, new Object[]{"str"})),
                "DescriptorSupport.equals(ImmutableDescriptor) true");
    }

    // ---- deepened: no-op mutations + union extras + isValid ------------------

    static void testNoOpsUnionIsValid() {
        final ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"a"}, new Object[]{"x"});
        // identical-value setField / absent removeField / null-name removeField: no exception
        d.setField("a", "x");
        d.setFields(new String[]{"a"}, new Object[]{"x"});
        d.removeField("absent");
        d.removeField(null);
        check(true, "no-op mutations do not throw");
        try { d.getFieldValue(null); check(false, "getFieldValue(null) should throw ROE"); }
        catch (RuntimeOperationsException ex) {
            check(ex.getCause() instanceof IllegalArgumentException
                    && "Null or empty field name".equals(ex.getCause().getMessage()),
                    "getFieldValue(null) ROE(IAE) message"); }
        try { d.setFields(new String[]{"a"}, new Object[]{}); check(false, "diff sizes should throw"); }
        catch (RuntimeOperationsException ex) {
            check("Different array sizes".equals(ex.getCause().getMessage()), "setFields sizes ROE(IAE)"); }

        ImmutableDescriptor one = new ImmutableDescriptor("a=1", "b=2");
        check(ImmutableDescriptor.union(one) == one, "single non-empty input returned as-is");
        check(ImmutableDescriptor.union() == ImmutableDescriptor.EMPTY_DESCRIPTOR, "no args -> EMPTY");
        check(ImmutableDescriptor.union(null, one, null) == one, "null elements skipped");
        try { ImmutableDescriptor.union((Descriptor[]) null); check(false, "union(null) NPE"); }
        catch (NullPointerException ex) { passed++; }
        try {
            ImmutableDescriptor.union(new ImmutableDescriptor("a=1"), new ImmutableDescriptor("A=2"));
            check(false, "case-insensitive conflict should throw");
        } catch (IllegalArgumentException ex) { passed++; }
        check(new ImmutableDescriptor("a.b=c").isValid(), "ImmutableDescriptor.isValid always true ('.' name)");
        check(new ImmutableDescriptor(new String[]{"k"}, new Object[]{new Object()}).isValid(),
                "ImmutableDescriptor.isValid always true (Object value)");
    }

    // ---- deepened: EMPTY_DESCRIPTOR facts + serialization + array cloning ----

    static void testEmptySerializationArrays() throws Exception {
        ImmutableDescriptor e = ImmutableDescriptor.EMPTY_DESCRIPTOR;
        check("{}".equals(e.toString()), "EMPTY toString is {}");
        check(e.clone() == e, "EMPTY clone is itself");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() == e, "EMPTY serialization readResolve returns same instance");

        ImmutableDescriptor original = new ImmutableDescriptor(new String[]{"a", "b"}, new Object[]{"x", 5});
        bos.reset();
        new ObjectOutputStream(bos).writeObject(original);
        ImmutableDescriptor read = (ImmutableDescriptor) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(read.equals(original) && read.getClass() == ImmutableDescriptor.class,
                "round-trip keeps class and equality");
        // JDK-27 fixed the JDK-8 quirk where the transient hashCode cache stayed
        // 0 after deserialization: readResolve forces recomputation, so the
        // hashCode of equal descriptors now agrees (version difference).
        check(read.hashCode() == original.hashCode() && read.hashCode() != 0,
                "JDK-27: deserialized hashCode recomputed and consistent");
        try {
            read.setField("b", "y");
            check(false, "deserialized descriptor must stay immutable");
        } catch (RuntimeOperationsException ex) {
            check(ex.getCause() instanceof UnsupportedOperationException,
                    "deserialized setField still ROE(UOE)");
        }

        ImmutableDescriptor arr = new ImmutableDescriptor(new String[]{"arr"}, new Object[]{new int[]{1, 2}});
        int[] v1 = (int[]) arr.getFieldValue("arr");
        int[] v2 = (int[]) arr.getFieldValue("arr");
        check(v1 != v2, "array value cloned on each getFieldValue");
        v1[0] = 99;
        check(((int[]) arr.getFieldValue("arr"))[0] == 1, "returned clone mutation isolated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorMatrix();
        testGetFieldsFormat();
        testNoOpsUnionIsValid();
        testEmptySerializationArrays();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
