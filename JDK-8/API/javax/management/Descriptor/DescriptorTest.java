/*
 * @test
 * @summary javax.management.Descriptor conformance: interface reflection.
 *          Deepened: full method-set presence, contract demonstrated through
 *          both real implementations - ImmutableDescriptor (read-only side:
 *          ROE(UnsupportedOperationException) on mutation, case-insensitive
 *          lookup) and DescriptorSupport (mutable side: setField/setFields/
 *          removeField round-trip, clone independence, getFields "n=(v)"
 *          format, setField(null) ROE(IAE), strict isValid requiring name +
 *          descriptorType and rejecting '.' field names / non-immutable value
 *          types), cross-implementation equals in both directions.
 * @run main DescriptorTest
 */

import java.util.Arrays;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;

public class DescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- deepened helpers ----------------------------------------------------

    static void expectROE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected RuntimeOperationsException");
        } catch (RuntimeOperationsException ex) {
            passed++;
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": got " + t.getClass().getName(), t);
        }
    }

    public static void main(String[] args) throws Exception {
        // ---- interface structure (kept) ----
        Class<?> c = Class.forName("javax.management.Descriptor");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(Class.forName("java.lang.Cloneable").isAssignableFrom(c), "implements/extends java.lang.Cloneable");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getFieldNames"), "declares getFieldNames");
        // ---- deepened: full declared method set ----
        check(hasMethod(c, "getFieldValue"), "declares getFieldValue");
        check(hasMethod(c, "setField"), "declares setField");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getFieldValues"), "declares getFieldValues");
        check(hasMethod(c, "removeField"), "declares removeField");
        check(hasMethod(c, "setFields"), "declares setFields");
        check(hasMethod(c, "isValid"), "declares isValid");
        check(hasMethod(c, "hashCode"), "declares hashCode");

        testImmutableSide();
        testMutableSide();
        testCrossImplementationEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: read-only contract via ImmutableDescriptor ----------------

    static void testImmutableSide() {
        final Descriptor d = new ImmutableDescriptor("name=n", "description=d");
        check("n".equals(d.getFieldValue("name")), "getFieldValue");
        check(d.getFieldNames().length == 2, "getFieldNames");
        check(d.getFieldValues("name", "description").length == 2, "getFieldValues");
        check(d.getFields().length == 2, "getFields");
        check(d.isValid(), "isValid");
        check("n".equals(d.getFieldValue("NAME")), "field lookup is case-insensitive");
        Descriptor clone = (Descriptor) d.clone();
        check(clone.equals(d), "clone equals");
        expectROE(new Runnable() { public void run() { d.setField("x", "y"); } },
                "immutable setField -> RuntimeOperationsException");
        expectROE(new Runnable() { public void run() { d.removeField("name"); } },
                "immutable removeField -> RuntimeOperationsException");
    }

    // ---- deepened: mutable contract via DescriptorSupport --------------------

    static void testMutableSide() {
        Descriptor d = new DescriptorSupport();
        check(d.getFieldNames().length == 0, "fresh mutable descriptor is empty");
        d.setField("name", "Counter");
        check("Counter".equals(d.getFieldValue("name")), "setField/getFieldValue round-trip");
        d.setFields(new String[]{"a", "b"}, new Object[]{1, 2L});
        check(d.getFieldValue("a").equals(1) && d.getFieldValue("b").equals(2L), "setFields round-trip");
        check(Arrays.equals(new String[]{"a", "b", "name"}, d.getFieldNames()),
                "getFieldNames case-insensitive sorted");
        // getFields: String values raw, non-String values parenthesized
        check(Arrays.equals(new String[]{"a=(1)", "b=(2)", "name=Counter"}, d.getFields()),
                "getFields format (non-String parenthesized)");
        // removeField actually removes
        d.removeField("a");
        check(d.getFieldValue("a") == null && d.getFieldNames().length == 2, "removeField removes");
        // clone is independent for mutable descriptors
        Descriptor cl = (Descriptor) d.clone();
        check(cl.equals(d), "mutable clone equals original");
        cl.setField("extra", "v");
        check(d.getFieldValue("extra") == null, "mutating clone leaves original untouched");
        // illegal operations on the mutable implementation
        expectROE(new Runnable() { public void run() { new DescriptorSupport().setField(null, "v"); } },
                "setField(null name) -> RuntimeOperationsException");
        // DescriptorSupport validates known field values EAGERLY (at set/construction):
        // visibility must be 1..4, severity 0..6, log boolean-ish, persistPolicy in
        // its enumeration, numeric time fields >= -1, name/descriptorType Strings.
        expectROE(new Runnable() { public void run() { new DescriptorSupport(new String[]{"visibility=5"}); } },
                "visibility=5 -> ROE(IllegalArgumentException) at construction");
        expectROE(new Runnable() { public void run() { new DescriptorSupport(new String[]{"log=yes"}); } },
                "log=yes -> ROE at construction");
        expectROE(new Runnable() { public void run() {
            new DescriptorSupport(new String[]{"name"}, new Object[]{Integer.valueOf(5)}); } },
                "non-String name -> ROE at construction");
        check(new DescriptorSupport("name=n", "descriptorType=mbean", "visibility=4").isValid(),
                "visibility=4 accepted");
        check(new DescriptorSupport("name=n", "descriptorType=mbean", "log=true").isValid(),
                "log=true accepted");
        // strict isValid: requires non-empty name AND descriptorType fields
        check(!new DescriptorSupport().isValid(), "empty descriptor not valid");
        check(!new DescriptorSupport(new String[]{"name=n"}).isValid(), "missing descriptorType -> not valid");
        check(!new DescriptorSupport(new String[]{"descriptorType=mbean"}).isValid(), "missing name -> not valid");
        check(new DescriptorSupport("name=n", "descriptorType=mbean").isValid(),
                "name + descriptorType -> valid");
        // unknown field names (even containing '.') or arbitrary value types stay valid:
        // only the well-known fields listed above are value-checked
        check(new DescriptorSupport("name=n", "descriptorType=mbean", "a.b=x").isValid(),
                "'.' field name on unknown field is not rejected");
        check(new DescriptorSupport(new String[]{"name", "descriptorType", "k"},
                new Object[]{"n", "mbean", new Object()}).isValid(),
                "arbitrary Object value on unknown field is not rejected");
    }

    // ---- deepened: cross-implementation equals -------------------------------

    static void testCrossImplementationEquals() {
        Descriptor imm = new ImmutableDescriptor("name=n", "descriptorType=mbean");
        Descriptor mut = new DescriptorSupport(new String[]{"name", "descriptorType"},
                new Object[]{"n", "mbean"});
        check(imm.equals(mut), "immutable.equals(mutable) true");
        check(mut.equals(imm), "mutable.equals(immutable) true");
        check(imm.hashCode() == mut.hashCode(), "equal descriptors agree on hashCode across implementations");
        Descriptor other = new ImmutableDescriptor("name=OTHER");
        check(!imm.equals(other) && !other.equals(imm), "different values not equal");
    }
}
