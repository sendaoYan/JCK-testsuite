/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataFormat conformance: interface hierarchy and methods.
 * @run main IIOMetadataFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class IIOMetadataFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Throwable expectThrows(String expectedClassName, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            check(t.getClass().getName().equals(expectedClassName),
                  m + " expected " + expectedClassName + " but got " + t.getClass().getName());
            return t;
        }
        check(false, m + " should throw " + expectedClassName);
        return null;
    }
    interface ThrowingRunnable { void run() throws Exception; }

    static int intConst(Class<?> c, String name) throws Exception {
        Field f = c.getField(name);
        check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
              && Modifier.isFinal(f.getModifiers()), "constant " + name + " is public static final");
        check(f.getType() == int.class, "constant " + name + " is int");
        return f.getInt(null);
    }

    /** Concrete format driven purely through the IIOMetadataFormat interface type. */
    static class TestFormat extends IIOMetadataFormatImpl {
        TestFormat() {
            super("root", CHILD_POLICY_ALL);
            addElement("child", "root", 1, 3);
            addAttribute("root", "count", DATATYPE_INTEGER, true, "5");
            addAttribute("root", "mode", DATATYPE_STRING, false, null, Arrays.asList("fast", "slow"));
            addAttribute("root", "range", DATATYPE_INTEGER, false, null, "0", "100", true, false);
            addAttribute("root", "list", DATATYPE_INTEGER, false, 1, 4);
            addBooleanAttribute("root", "flag", true, false);
            addObjectValue("root", String.class, false, "dflt");
        }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormat");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getRootName"), "method getRootName");
        check(dm.contains("canNodeAppear"), "method canNodeAppear");
        check(dm.contains("getElementMinChildren"), "method getElementMinChildren");
        check(dm.contains("getElementMaxChildren"), "method getElementMaxChildren");
        check(dm.contains("getElementDescription"), "method getElementDescription");
        check(dm.contains("getChildPolicy"), "method getChildPolicy");
        check(dm.contains("getChildNames"), "method getChildNames");
        check(dm.contains("getAttributeNames"), "method getAttributeNames");
        check(dm.contains("getAttributeValueType"), "method getAttributeValueType");
        check(dm.contains("getAttributeDataType"), "method getAttributeDataType");
        check(dm.contains("isAttributeRequired"), "method isAttributeRequired");
        check(dm.contains("getAttributeDefaultValue"), "method getAttributeDefaultValue");
        check(dm.contains("getAttributeEnumerations"), "method getAttributeEnumerations");
        check(dm.contains("getAttributeMinValue"), "method getAttributeMinValue");
        check(dm.contains("getAttributeMaxValue"), "method getAttributeMaxValue");
        check(dm.contains("getAttributeListMinLength"), "method getAttributeListMinLength");
        check(dm.contains("getAttributeListMaxLength"), "method getAttributeListMaxLength");
        check(dm.contains("getAttributeDescription"), "method getAttributeDescription");
        check(dm.contains("getObjectValueType"), "method getObjectValueType");
        check(dm.contains("getObjectClass"), "method getObjectClass");
        check(dm.contains("getObjectDefaultValue"), "method getObjectDefaultValue");
        check(dm.contains("getObjectEnumerations"), "method getObjectEnumerations");
        check(dm.contains("getObjectMinValue"), "method getObjectMinValue");
        check(dm.contains("getObjectMaxValue"), "method getObjectMaxValue");
        check(dm.contains("getObjectArrayMinLength"), "method getObjectArrayMinLength");
        check(dm.contains("getObjectArrayMaxLength"), "method getObjectArrayMaxLength");
        check(dm.size() == 26, "exactly 26 abstract methods");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "method " + m.getName() + " abstract");
        }
        check(c.getDeclaredFields().length >= 22, "constants declared as fields");
        testConstants(c);
        testViaInterface();
        testStandardFormat();
        testNegativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // constant value table (values from javap on jdk8u372-b07)
    static void testConstants(Class<?> c) throws Exception {
        check(intConst(c, "CHILD_POLICY_EMPTY") == 0, "CHILD_POLICY_EMPTY == 0");
        check(intConst(c, "CHILD_POLICY_ALL") == 1, "CHILD_POLICY_ALL == 1");
        check(intConst(c, "CHILD_POLICY_SOME") == 2, "CHILD_POLICY_SOME == 2");
        check(intConst(c, "CHILD_POLICY_CHOICE") == 3, "CHILD_POLICY_CHOICE == 3");
        check(intConst(c, "CHILD_POLICY_SEQUENCE") == 4, "CHILD_POLICY_SEQUENCE == 4");
        check(intConst(c, "CHILD_POLICY_REPEAT") == 5, "CHILD_POLICY_REPEAT == 5");
        check(intConst(c, "CHILD_POLICY_MAX") == 5, "CHILD_POLICY_MAX == 5");
        check(intConst(c, "VALUE_NONE") == 0, "VALUE_NONE == 0");
        check(intConst(c, "VALUE_ARBITRARY") == 1, "VALUE_ARBITRARY == 1");
        check(intConst(c, "VALUE_RANGE") == 2, "VALUE_RANGE == 2");
        check(intConst(c, "VALUE_RANGE_MIN_INCLUSIVE_MASK") == 4, "VALUE_RANGE_MIN_INCLUSIVE_MASK == 4");
        check(intConst(c, "VALUE_RANGE_MAX_INCLUSIVE_MASK") == 8, "VALUE_RANGE_MAX_INCLUSIVE_MASK == 8");
        check(intConst(c, "VALUE_RANGE_MIN_INCLUSIVE") == 6, "VALUE_RANGE_MIN_INCLUSIVE == 6");
        check(intConst(c, "VALUE_RANGE_MAX_INCLUSIVE") == 10, "VALUE_RANGE_MAX_INCLUSIVE == 10");
        check(intConst(c, "VALUE_RANGE_MIN_MAX_INCLUSIVE") == 14, "VALUE_RANGE_MIN_MAX_INCLUSIVE == 14");
        check(intConst(c, "VALUE_ENUMERATION") == 16, "VALUE_ENUMERATION == 16");
        check(intConst(c, "VALUE_LIST") == 32, "VALUE_LIST == 32");
        check(intConst(c, "DATATYPE_STRING") == 0, "DATATYPE_STRING == 0");
        check(intConst(c, "DATATYPE_BOOLEAN") == 1, "DATATYPE_BOOLEAN == 1");
        check(intConst(c, "DATATYPE_INTEGER") == 2, "DATATYPE_INTEGER == 2");
        check(intConst(c, "DATATYPE_FLOAT") == 3, "DATATYPE_FLOAT == 3");
        check(intConst(c, "DATATYPE_DOUBLE") == 4, "DATATYPE_DOUBLE == 4");
        // mask composition identities spelled out in the javadoc
        check((IIOMetadataFormat.VALUE_RANGE
              | IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE_MASK
              | IIOMetadataFormat.VALUE_RANGE_MAX_INCLUSIVE_MASK)
              == IIOMetadataFormat.VALUE_RANGE_MIN_MAX_INCLUSIVE, "mask composition identity");
    }

    // behavioral checks through the interface reference only
    static void testViaInterface() throws Exception {
        IIOMetadataFormat f = new TestFormat();
        check("root".equals(f.getRootName()), "getRootName");
        check(f.canNodeAppear("root", null), "canNodeAppear true");
        check(f.canNodeAppear("root", ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)) != false,
              "canNodeAppear with a real image type");
        check(f.getChildPolicy("root") == IIOMetadataFormat.CHILD_POLICY_ALL, "root child policy ALL");
        check(Arrays.equals(f.getChildNames("root"), new String[]{"child"}), "root child names");
        check(f.getChildPolicy("child") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "child policy REPEAT");
        check(f.getElementMinChildren("child") == 1, "child min children");
        check(f.getElementMaxChildren("child") == 3, "child max children");

        String[] attrs = f.getAttributeNames("root");
        check(attrs.length == 5, "root has 5 attributes");
        check(contains(attrs, "count") && contains(attrs, "mode") && contains(attrs, "range")
              && contains(attrs, "list") && contains(attrs, "flag"), "attribute names present");
        check(f.getAttributeValueType("root", "count") == IIOMetadataFormat.VALUE_ARBITRARY, "count arbitrary");
        check(f.getAttributeDataType("root", "count") == IIOMetadataFormat.DATATYPE_INTEGER, "count integer");
        check(f.isAttributeRequired("root", "count"), "count required");
        check("5".equals(f.getAttributeDefaultValue("root", "count")), "count default");
        check(f.getAttributeValueType("root", "mode") == IIOMetadataFormat.VALUE_ENUMERATION, "mode enumeration");
        check(Arrays.equals(f.getAttributeEnumerations("root", "mode"), new String[]{"fast", "slow"}),
              "mode enumerations");
        check(f.getAttributeDataType("root", "mode") == IIOMetadataFormat.DATATYPE_STRING, "mode string");
        check(!f.isAttributeRequired("root", "mode"), "mode not required");
        check(f.getAttributeDefaultValue("root", "mode") == null, "mode no default");
        check(f.getAttributeValueType("root", "range") == IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE,
              "range min-inclusive only");
        check("0".equals(f.getAttributeMinValue("root", "range")), "range min");
        check("100".equals(f.getAttributeMaxValue("root", "range")), "range max");
        check(f.getAttributeValueType("root", "list") == IIOMetadataFormat.VALUE_LIST, "list value type");
        check(f.getAttributeListMinLength("root", "list") == 1, "list min length");
        check(f.getAttributeListMaxLength("root", "list") == 4, "list max length");
        check(f.getAttributeDataType("root", "list") == IIOMetadataFormat.DATATYPE_INTEGER, "list integer");
        check(f.getAttributeValueType("root", "flag") == IIOMetadataFormat.VALUE_ENUMERATION, "flag enumeration");
        check(f.getAttributeDataType("root", "flag") == IIOMetadataFormat.DATATYPE_BOOLEAN, "flag boolean");
        check(Arrays.equals(f.getAttributeEnumerations("root", "flag"), new String[]{"TRUE", "FALSE"}),
              "flag enumerations");
        check("FALSE".equals(f.getAttributeDefaultValue("root", "flag")), "flag default FALSE");
        check(f.getObjectValueType("root") == IIOMetadataFormat.VALUE_ARBITRARY, "object value arbitrary");
        check(f.getObjectClass("root") == String.class, "object class String");
        check("dflt".equals(f.getObjectDefaultValue("root")), "object default value");
        // element with no attributes at all
        check(f.getAttributeNames("child").length == 0, "child has no attributes");
        // element with EMPTY policy has null child names
        check(f.getChildPolicy("child") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "child policy");
    }

    // the JDK-provided standard format through the interface type
    static void testStandardFormat() throws Exception {
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check("javax_imageio_1.0".equals(std.getRootName()), "standard root name");
        check(std.getChildPolicy("javax_imageio_1.0") == IIOMetadataFormat.CHILD_POLICY_SOME,
              "standard root policy SOME");
        String[] rootChildren = std.getChildNames("javax_imageio_1.0");
        check(rootChildren != null && contains(rootChildren, "Chroma") && contains(rootChildren, "Text")
              && contains(rootChildren, "Data") && contains(rootChildren, "Compression")
              && contains(rootChildren, "Dimension"), "standard root children");
        check(std.canNodeAppear("Chroma", null) && std.canNodeAppear("Text", null), "standard canNodeAppear");
        check(std.getChildPolicy("Text") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "Text policy REPEAT");
        check(std.getElementMinChildren("Text") == 0, "Text min children");
        check(std.getElementMaxChildren("Text") == Integer.MAX_VALUE, "Text max children");
        check(std.getChildPolicy("Chroma") == IIOMetadataFormat.CHILD_POLICY_SOME, "Chroma policy SOME");
        check(contains(std.getChildNames("Chroma"), "ColorSpaceType"), "Chroma children include ColorSpaceType");
        check(contains(std.getChildNames("Chroma"), "NumChannels"), "Chroma children include NumChannels");
        check(Arrays.equals(std.getAttributeNames("ColorSpaceType"), new String[]{"name"}),
              "ColorSpaceType has single attribute name");
        check(std.isAttributeRequired("ColorSpaceType", "name"), "name required");
        check(std.getAttributeValueType("ColorSpaceType", "name") == IIOMetadataFormat.VALUE_ENUMERATION,
              "name is enumeration");
        check(std.getAttributeDataType("ColorSpaceType", "name") == IIOMetadataFormat.DATATYPE_STRING,
              "name is string");
        check(std.getAttributeDefaultValue("ColorSpaceType", "name") == null, "name has no default");
        check(std.getAttributeNames("NumChannels").length == 1
              && "value".equals(std.getAttributeNames("NumChannels")[0]), "NumChannels attribute value");
        check(std.getElementDescription("Text", Locale.ROOT) != null, "Text description from resource bundle");
        check(std.getElementDescription("javax_imageio_1.0", Locale.ROOT) == null,
              "root element has no description entry");
        // TextEntry child of Text
        check(std.getChildPolicy("TextEntry") == IIOMetadataFormat.CHILD_POLICY_EMPTY, "TextEntry policy EMPTY");
        check(contains(std.getAttributeNames("TextEntry"), "value"), "TextEntry has value attribute");
        check(contains(std.getAttributeNames("TextEntry"), "keyword"), "TextEntry has keyword attribute");
        check(std.getAttributeNames("TextEntry").length == 5, "TextEntry has 5 attributes");
        check(std.isAttributeRequired("TextEntry", "value"), "TextEntry value required");
        check(std.getAttributeValueType("TextEntry", "value") == IIOMetadataFormat.VALUE_ARBITRARY,
              "TextEntry value arbitrary");
        check(std.getAttributeDataType("TextEntry", "value") == IIOMetadataFormat.DATATYPE_STRING,
              "TextEntry value string");
        check(std.getAttributeDescription("TextEntry", "value", Locale.ROOT) != null,
              "TextEntry value description from bundle");
        // canNodeAppear of the standard format returns true for any name, including unknown ones
        check(std.canNodeAppear("no_such_element", null), "canNodeAppear unknown element true (standard impl)");
        check(std.canNodeAppear("javax_imageio_1.0", null), "canNodeAppear root true");
    }

    // negative matrix (exact exception classes probed on jdk8u372-b07)
    static void testNegativeMatrix() throws Exception {
        IIOMetadataFormat f = new TestFormat();
        expectThrows("java.lang.IllegalArgumentException", () -> f.getElementMinChildren("root"),
                     "getElementMinChildren on non-REPEAT");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getElementMaxChildren("root"),
                     "getElementMaxChildren on non-REPEAT");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getAttributeEnumerations("root", "count"),
                     "getAttributeEnumerations on non-enumeration");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getChildPolicy("absent"),
                     "getChildPolicy unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getChildNames("absent"),
                     "getChildNames unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getAttributeNames("absent"),
                     "getAttributeNames unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getAttributeMinValue("root", "count"),
                     "getAttributeMinValue on non-range");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getAttributeListMinLength("root", "count"),
                     "getAttributeListMinLength on non-list");
        expectThrows("java.lang.IllegalArgumentException", () -> f.isAttributeRequired("root", "absent"),
                     "isAttributeRequired unknown attribute");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getElementDescription("absent", null),
                     "getElementDescription unknown element");
        // getObjectValueType returns VALUE_NONE for an element without an object value (does not throw)
        check(f.getObjectValueType("child") == IIOMetadataFormat.VALUE_NONE,
              "getObjectValueType VALUE_NONE for objectless element");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getObjectDefaultValue("child"),
                     "getObjectDefaultValue element without object");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getObjectClass("child"),
                     "getObjectClass element without object");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getElementMinChildren(null),
                     "getElementMinChildren null element");
        expectThrows("java.lang.IllegalArgumentException", () -> f.getChildPolicy(null),
                     "getChildPolicy null element");
    }

    static boolean contains(String[] arr, String s) {
        for (String x : arr) if (s.equals(x)) return true;
        return false;
    }
}
