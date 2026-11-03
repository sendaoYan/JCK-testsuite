/*
 * @test
 * @summary javax.management.ImmutableDescriptor conformance: white-box structure + black-box behavior.
 * @run main ImmutableDescriptorTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
