/*
 * @test
 * @summary javax.management.modelmbean.DescriptorSupport conformance: white-box structure + black-box behavior.
 * @run main DescriptorSupportTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.Descriptor;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;

public class DescriptorSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static String[] sorted(String[] in) {
        String[] a = in.clone();
        Arrays.sort(a);
        return a;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.DescriptorSupport");
        check(!c.isInterface(), "not interface");
        check(Descriptor.class.isAssignableFrom(c), "implements Descriptor");
        check(c.getConstructors().length >= 5, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Construction + setField + getFieldValue
        DescriptorSupport d = new DescriptorSupport();
        check(d.getFieldNames().length == 0, "empty descriptor starts empty");
        check(d.getFieldValue("absent") == null, "getFieldValue absent returns null");

        d.setField("name", "myMBean");
        check("myMBean".equals(d.getFieldValue("name")), "setField/getFieldValue String");
        check(d.getFieldNames().length == 1, "one field name after setField");

        d.setField("count", 42);
        check(Integer.valueOf(42).equals(d.getFieldValue("count")), "setField/getFieldValue Integer");

        // case-insensitive field name lookup
        check("myMBean".equals(d.getFieldValue("NAME")), "getFieldValue case-insensitive");

        // setFields bulk
        d.setFields(new String[]{"a", "b", "c"}, new Object[]{1, "two", 3L});
        check(Integer.valueOf(1).equals(d.getFieldValue("a")), "setFields a");
        check("two".equals(d.getFieldValue("b")), "setFields b");
        check(Long.valueOf(3L).equals(d.getFieldValue("c")), "setFields c");

        // getFieldNames returns sorted (case-insensitive) set
        String[] names = sorted(d.getFieldNames());
        Set<String> expected = new HashSet<>(Arrays.asList("name", "count", "a", "b", "c"));
        Set<String> actual = new HashSet<>(Arrays.asList(names));
        check(actual.equals(expected), "getFieldNames returns all field names");
        check(names.length == 5, "five field names total");

        // getFieldValues
        Object[] vals = d.getFieldValues("name", "count");
        check(vals.length == 2 && "myMBean".equals(vals[0]) && Integer.valueOf(42).equals(vals[1]),
                "getFieldValues by name array");
        Object[] all = d.getFieldValues((String[]) null);
        check(all.length == 5, "getFieldValues(null) returns all values");
        Object[] none = d.getFieldValues();
        check(none.length == 0, "getFieldValues() returns empty");

        // getFields produces key=value / key=(value)
        boolean sawString = false, sawWrapped = false;
        for (String f : d.getFields()) {
            if (f.equals("name=myMBean")) sawString = true;
            if (f.equals("count=(42)")) sawWrapped = true;
        }
        check(sawString, "getFields String format");
        check(sawWrapped, "getFields non-String format");

        // clone is deep-equal and independent
        DescriptorSupport clone = (DescriptorSupport) d.clone();
        check(clone.equals(d), "clone equals original");
        check(clone.hashCode() == d.hashCode(), "clone hashCode equals original");
        clone.setField("name", "changed");
        check("myMBean".equals(d.getFieldValue("name")), "original unaffected by clone mutation");
        check("changed".equals(clone.getFieldValue("name")), "clone mutation applied");

        // equals: case-insensitive on field names, value-sensitive
        DescriptorSupport same = new DescriptorSupport();
        same.setField("NAME", "myMBean");
        same.setField("COUNT", 42);
        same.setField("a", 1);
        same.setField("b", "two");
        same.setField("c", 3L);
        check(d.equals(same), "equals ignores field-name case");
        DescriptorSupport diff = new DescriptorSupport();
        diff.setField("name", "other");
        check(!d.equals(diff), "not equals for different value");
        check(!d.equals(null), "not equals null");
        check(!d.equals("a string"), "not equals non-Descriptor");

        // removeField
        d.removeField("a");
        check(d.getFieldValue("a") == null, "removeField removes value");
        check(sorted(d.getFieldNames()).length == 4, "removeField shrinks field set");
        d.removeField(null); // no-op
        d.removeField("");   // no-op
        check(sorted(d.getFieldNames()).length == 4, "removeField null/empty no-op");

        // toString joins fields with ", "
        String str = d.toString();
        check(str.contains("name=myMBean"), "toString contains String field");
        check(str.contains("count=(42)"), "toString contains wrapped field");
        DescriptorSupport empty = new DescriptorSupport();
        check("".equals(empty.toString()), "toString empty descriptor");

        // isValid: minimal descriptor with name + descriptorType is valid
        DescriptorSupport valid = new DescriptorSupport();
        valid.setField("name", "x");
        valid.setField("descriptorType", "mbean");
        check(valid.isValid(), "isValid with name+descriptorType");

        // Exception cases
        boolean re1 = false;
        try { d.getFieldValue(null); } catch (RuntimeOperationsException e) { re1 = true; }
        check(re1, "getFieldValue(null) throws RuntimeOperationsException");

        boolean re2 = false;
        try { d.setField(null, "v"); } catch (RuntimeOperationsException e) { re2 = true; }
        check(re2, "setField(null,..) throws RuntimeOperationsException");

        boolean re3 = false;
        try { d.setField("", "v"); } catch (RuntimeOperationsException e) { re3 = true; }
        check(re3, "setField(empty,..) throws RuntimeOperationsException");

        boolean re4 = false;
        try { d.setFields(new String[]{"x"}, new Object[]{1, 2}); } catch (RuntimeOperationsException e) { re4 = true; }
        check(re4, "setFields length mismatch throws RuntimeOperationsException");

        // Constructor from String[]/Object[]
        DescriptorSupport fromArrays = new DescriptorSupport(
                new String[]{"name", "type"}, new Object[]{"n", "t"});
        check("n".equals(fromArrays.getFieldValue("name")), "array constructor name");
        check("t".equals(fromArrays.getFieldValue("type")), "array constructor type");

        // Constructor from varargs String...
        DescriptorSupport fromVarargs = new DescriptorSupport("name=v1", "color=blue");
        check("v1".equals(fromVarargs.getFieldValue("name")), "varargs constructor name");
        check("blue".equals(fromVarargs.getFieldValue("color")), "varargs constructor color");

        // Copy constructor
        DescriptorSupport copied = new DescriptorSupport(fromVarargs);
        check(copied.equals(fromVarargs), "copy constructor equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
