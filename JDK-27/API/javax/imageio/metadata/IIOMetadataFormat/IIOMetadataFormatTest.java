/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataFormat conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataFormatTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class IIOMetadataFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }
    static int intConst(Class<?> c, String name) throws Exception {
        Field f = c.getField(name);
        check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
              && Modifier.isFinal(f.getModifiers()), "constant " + name + " is public static final");
        check(f.getType() == int.class, "constant " + name + " is int");
        return f.getInt(null);
    }

    // minimal concrete format used purely through the interface type
    static class TestFormat extends IIOMetadataFormatImpl {
        TestFormat() {
            super("root", CHILD_POLICY_ALL);
            addElement("child", "root", 1, 3);
            addAttribute("root", "count", DATATYPE_INTEGER, true, "5");
            addAttribute("root", "mode", DATATYPE_STRING, false, null, List.of("fast", "slow"));
            addAttribute("root", "range", DATATYPE_INTEGER, false, null, "0", "100", true, false);
            addAttribute("root", "list", DATATYPE_INTEGER, false, 1, 4);
            addBooleanAttribute("root", "flag", true, false);
            addObjectValue("root", String.class, false, "dflt");
        }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormat");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public modifier");
        check(Modifier.isAbstract(c.getModifiers()), "abstract modifier");
        // child policy constants
        check(intConst(c, "CHILD_POLICY_EMPTY") == 0, "CHILD_POLICY_EMPTY == 0");
        check(intConst(c, "CHILD_POLICY_ALL") == 1, "CHILD_POLICY_ALL == 1");
        check(intConst(c, "CHILD_POLICY_SOME") == 2, "CHILD_POLICY_SOME == 2");
        check(intConst(c, "CHILD_POLICY_CHOICE") == 3, "CHILD_POLICY_CHOICE == 3");
        check(intConst(c, "CHILD_POLICY_SEQUENCE") == 4, "CHILD_POLICY_SEQUENCE == 4");
        check(intConst(c, "CHILD_POLICY_REPEAT") == 5, "CHILD_POLICY_REPEAT == 5");
        check(intConst(c, "CHILD_POLICY_MAX") == 5, "CHILD_POLICY_MAX == 5");
        // value type constants
        check(intConst(c, "VALUE_NONE") == 0, "VALUE_NONE == 0");
        check(intConst(c, "VALUE_ARBITRARY") == 1, "VALUE_ARBITRARY == 1");
        check(intConst(c, "VALUE_RANGE") == 2, "VALUE_RANGE == 2");
        check(intConst(c, "VALUE_ENUMERATION") == 16, "VALUE_ENUMERATION == 16");
        check(intConst(c, "VALUE_LIST") == 32, "VALUE_LIST == 32");
        // datatype constants
        check(intConst(c, "DATATYPE_STRING") == 0, "DATATYPE_STRING == 0");
        check(intConst(c, "DATATYPE_BOOLEAN") == 1, "DATATYPE_BOOLEAN == 1");
        check(intConst(c, "DATATYPE_INTEGER") == 2, "DATATYPE_INTEGER == 2");
        check(intConst(c, "DATATYPE_FLOAT") == 3, "DATATYPE_FLOAT == 3");
        check(intConst(c, "DATATYPE_DOUBLE") == 4, "DATATYPE_DOUBLE == 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // exercise a concrete format exclusively through the interface type
        IIOMetadataFormat f = new TestFormat();
        check("root".equals(f.getRootName()), "getRootName");
        check(f.canNodeAppear("root", null), "canNodeAppear true");
        check(f.getChildPolicy("root") == IIOMetadataFormat.CHILD_POLICY_ALL, "root child policy ALL");
        check(Arrays.equals(f.getChildNames("root"), new String[]{"child"}), "root child names");

        // REPEAT child multiplicity
        check(f.getChildPolicy("child") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "child policy REPEAT");
        check(f.getElementMinChildren("child") == 1, "child min children");
        check(f.getElementMaxChildren("child") == 3, "child max children");

        // attributes: arbitrary / enumeration / range / list / boolean
        String[] attrs = f.getAttributeNames("root");
        check(attrs.length == 5, "root has 5 attributes");
        check(contains(attrs, "count") && contains(attrs, "mode") && contains(attrs, "range")
              && contains(attrs, "list") && contains(attrs, "flag"), "attribute names present");

        check(f.getAttributeValueType("root", "count") == IIOMetadataFormat.VALUE_ARBITRARY, "count arbitrary");
        check(f.getAttributeDataType("root", "count") == IIOMetadataFormat.DATATYPE_INTEGER, "count integer");
        check(f.isAttributeRequired("root", "count"), "count required");
        check("5".equals(f.getAttributeDefaultValue("root", "count")), "count default");

        check(f.getAttributeValueType("root", "mode") == IIOMetadataFormat.VALUE_ENUMERATION, "mode enumeration");
        check(Arrays.equals(f.getAttributeEnumerations("root", "mode"), new String[]{"fast", "slow"}), "mode enumerations");
        check(f.getAttributeDataType("root", "mode") == IIOMetadataFormat.DATATYPE_STRING, "mode string");

        check(f.getAttributeValueType("root", "range") == IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE, "range min-inclusive");
        check("0".equals(f.getAttributeMinValue("root", "range")), "range min");
        check("100".equals(f.getAttributeMaxValue("root", "range")), "range max");

        check(f.getAttributeValueType("root", "list") == IIOMetadataFormat.VALUE_LIST, "list value type");
        check(f.getAttributeListMinLength("root", "list") == 1, "list min length");
        check(f.getAttributeListMaxLength("root", "list") == 4, "list max length");

        check(f.getAttributeValueType("root", "flag") == IIOMetadataFormat.VALUE_ENUMERATION, "flag enumeration");
        check(f.getAttributeDataType("root", "flag") == IIOMetadataFormat.DATATYPE_BOOLEAN, "flag boolean");
        check(Arrays.equals(f.getAttributeEnumerations("root", "flag"), new String[]{"TRUE", "FALSE"}), "flag enumerations");
        check("FALSE".equals(f.getAttributeDefaultValue("root", "flag")), "flag default FALSE");

        // object value: arbitrary
        check(f.getObjectValueType("root") == IIOMetadataFormat.VALUE_ARBITRARY, "object value arbitrary");
        check(f.getObjectClass("root") == String.class, "object class String");
        check("dflt".equals(f.getObjectDefaultValue("root")), "object default value");

        // the real standard format instance through the interface type
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check("javax_imageio_1.0".equals(std.getRootName()), "standard root name");
        check(std.getChildPolicy("javax_imageio_1.0") == IIOMetadataFormat.CHILD_POLICY_SOME, "standard root policy SOME");
        String[] rootChildren = std.getChildNames("javax_imageio_1.0");
        check(rootChildren != null && contains(rootChildren, "Chroma") && contains(rootChildren, "Text"),
              "standard root children");
        check(std.canNodeAppear("Chroma", null) && std.canNodeAppear("Text", null), "standard canNodeAppear");
        check(std.getChildPolicy("Text") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "Text policy REPEAT");
        check(std.getElementMinChildren("Text") == 0, "Text min children");
        check(std.getElementMaxChildren("Text") == Integer.MAX_VALUE, "Text max children");

        // contract exceptions
        expectThrows(IllegalArgumentException.class, () -> f.getElementMinChildren("root"),
                     "getElementMinChildren on non-REPEAT");
        expectThrows(IllegalArgumentException.class, () -> f.getAttributeEnumerations("root", "count"),
                     "getAttributeEnumerations on non-enumeration");
        expectThrows(IllegalArgumentException.class, () -> f.getChildPolicy("absent"),
                     "getChildPolicy unknown element");
    }

    static boolean contains(String[] arr, String s) {
        for (String x : arr) if (s.equals(x)) return true;
        return false;
    }

    // ---- Part 3: additional range-mask constants + method set (probed on jdk-27.0.1) ----
    static void testConstantsAndSignatures() throws Exception {
        Class<?> c = IIOMetadataFormat.class;
        check(intConst(c, "VALUE_RANGE_MIN_INCLUSIVE_MASK") == 4, "VALUE_RANGE_MIN_INCLUSIVE_MASK == 4");
        check(intConst(c, "VALUE_RANGE_MAX_INCLUSIVE_MASK") == 8, "VALUE_RANGE_MAX_INCLUSIVE_MASK == 8");
        check(intConst(c, "VALUE_RANGE_MIN_INCLUSIVE") == 6, "VALUE_RANGE_MIN_INCLUSIVE == 6");
        check(intConst(c, "VALUE_RANGE_MAX_INCLUSIVE") == 10, "VALUE_RANGE_MAX_INCLUSIVE == 10");
        check(intConst(c, "VALUE_RANGE_MIN_MAX_INCLUSIVE") == 14, "VALUE_RANGE_MIN_MAX_INCLUSIVE == 14");
        check((IIOMetadataFormat.VALUE_RANGE
              | IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE_MASK
              | IIOMetadataFormat.VALUE_RANGE_MAX_INCLUSIVE_MASK)
              == IIOMetadataFormat.VALUE_RANGE_MIN_MAX_INCLUSIVE, "mask composition identity");
        // exactly 26 abstract methods, all abstract and public
        check(c.getDeclaredMethods().length == 26, "exactly 26 abstract methods");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "method " + m.getName() + " abstract");
        }
    }

    // ---- Part 4: standard format extra details + wider negative matrix ----
    static void testStandardDetailsAndNegatives() throws Exception {
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check(std.getChildPolicy("Chroma") == IIOMetadataFormat.CHILD_POLICY_SOME, "Chroma policy SOME");
        check(contains(std.getChildNames("Chroma"), "ColorSpaceType"), "Chroma children include ColorSpaceType");
        check(java.util.Arrays.equals(std.getAttributeNames("ColorSpaceType"), new String[]{"name"}),
              "ColorSpaceType single attribute name");
        check(std.isAttributeRequired("ColorSpaceType", "name"), "name required");
        check(std.getAttributeValueType("ColorSpaceType", "name") == IIOMetadataFormat.VALUE_ENUMERATION,
              "name is enumeration");
        check(std.getAttributeDataType("ColorSpaceType", "name") == IIOMetadataFormat.DATATYPE_STRING,
              "name is string");
        check(std.getAttributeDefaultValue("ColorSpaceType", "name") == null, "name has no default");
        check(std.getAttributeNames("NumChannels").length == 1
              && "value".equals(std.getAttributeNames("NumChannels")[0]), "NumChannels attribute value");
        check(std.getChildPolicy("TextEntry") == IIOMetadataFormat.CHILD_POLICY_EMPTY, "TextEntry policy EMPTY");
        check(std.getAttributeNames("TextEntry").length == 5, "TextEntry has 5 attributes");
        check(contains(std.getAttributeNames("TextEntry"), "keyword"), "TextEntry keyword attribute");
        check(std.isAttributeRequired("TextEntry", "value"), "TextEntry value required");
        check(std.getAttributeValueType("TextEntry", "value") == IIOMetadataFormat.VALUE_ARBITRARY,
              "TextEntry value arbitrary");
        check(std.getAttributeDescription("TextEntry", "value", java.util.Locale.ROOT) != null,
              "TextEntry value description from bundle");
        check(std.getElementDescription("javax_imageio_1.0", java.util.Locale.ROOT) == null,
              "root element has no description entry");
        // canNodeAppear of the standard format returns true for any name, including unknown ones
        check(std.canNodeAppear("no_such_element", null), "canNodeAppear unknown element true (standard impl)");

        IIOMetadataFormat f = new TestFormat();
        expectThrows(IllegalArgumentException.class, () -> f.getElementMaxChildren("root"),
                     "getElementMaxChildren on non-REPEAT");
        expectThrows(IllegalArgumentException.class, () -> f.getChildNames("absent"),
                     "getChildNames unknown element");
        expectThrows(IllegalArgumentException.class, () -> f.getAttributeNames("absent"),
                     "getAttributeNames unknown element");
        expectThrows(IllegalArgumentException.class, () -> f.getAttributeMinValue("root", "count"),
                     "getAttributeMinValue on non-range");
        expectThrows(IllegalArgumentException.class, () -> f.getAttributeListMinLength("root", "count"),
                     "getAttributeListMinLength on non-list");
        expectThrows(IllegalArgumentException.class, () -> f.isAttributeRequired("root", "absent"),
                     "isAttributeRequired unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> f.getElementDescription("absent", null),
                     "getElementDescription unknown element");
        expectThrows(IllegalArgumentException.class, () -> f.getChildPolicy(null),
                     "getChildPolicy null element");
        expectThrows(IllegalArgumentException.class, () -> f.getObjectDefaultValue("child"),
                     "getObjectDefaultValue element without object");
        expectThrows(IllegalArgumentException.class, () -> f.getObjectClass("child"),
                     "getObjectClass element without object");
        // getObjectValueType reports VALUE_NONE instead of throwing for objectless elements
        check(f.getObjectValueType("child") == IIOMetadataFormat.VALUE_NONE,
              "getObjectValueType VALUE_NONE for objectless element");
        check(f.getAttributeNames("child").length == 0, "child has no attributes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantsAndSignatures();
        testStandardDetailsAndNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
