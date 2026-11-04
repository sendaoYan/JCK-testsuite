/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataFormatImpl conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataFormatImplTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class IIOMetadataFormatImplTest {
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

    // richer concrete subclass exercising the protected add*/remove* builder methods
    static class TestImpl extends IIOMetadataFormatImpl {
        TestImpl() {
            super("root", CHILD_POLICY_SEQUENCE);
            addElement("item", "root", 1, 4);
            addElement("meta", "item", CHILD_POLICY_EMPTY);
            addChildElement("meta", "root");
            addAttribute("item", "name", DATATYPE_STRING, true, "unnamed");
            addBooleanAttribute("item", "active", true, true);
            addBooleanAttribute("item", "hidden", false, false);
            addObjectValue("item", Integer.class, true, Integer.valueOf(1));
            addObjectValue("meta", String.class, false, null, List.of("x", "y"));
            addObjectValue("root", int.class, 0, 3);
        }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
        // expose protected mutators for white/black-box observation
        public void removeAttr(String e, String a) { removeAttribute(e, a); }
        public void removeElem(String e) { removeElement(e); }
        public void addAttrList(String e, String a, int t, boolean r, int min, int max) {
            addAttribute(e, a, t, r, min, max);
        }
    }

    // subclass whose constructor throws for validation checks
    static class BadRoot extends IIOMetadataFormatImpl {
        BadRoot(String root, int policy) { super(root, policy); }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }
    static class BadRepeat extends IIOMetadataFormatImpl {
        BadRepeat(String root, int min, int max) { super(root, min, max); }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormatImpl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is not interface");
        check(!c.isAnnotation(), "is not annotation");
        check(!c.isEnum(), "is not enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0].getName().equals("javax.imageio.metadata.IIOMetadataFormat"),
              "implements IIOMetadataFormat");
        // standardMetadataFormatName constant
        Field nameField = c.getField("standardMetadataFormatName");
        check(Modifier.isPublic(nameField.getModifiers()) && Modifier.isStatic(nameField.getModifiers())
              && Modifier.isFinal(nameField.getModifiers()), "standardMetadataFormatName public static final");
        check("javax_imageio_1.0".equals(nameField.get(null)), "standardMetadataFormatName value");
        // static factory
        Method factory = c.getMethod("getStandardFormatInstance");
        check(Modifier.isPublic(factory.getModifiers()) && Modifier.isStatic(factory.getModifiers()),
              "getStandardFormatInstance public static");
        // protected constructors and builder methods exist
        check(c.getDeclaredConstructor(String.class, int.class) != null, "ctor (String,int)");
        check(c.getDeclaredConstructor(String.class, int.class, int.class) != null, "ctor (String,int,int)");
        check(c.getDeclaredMethod("addElement", String.class, String.class, int.class) != null, "addElement");
        check(c.getDeclaredMethod("addAttribute", String.class, String.class, int.class, boolean.class, String.class) != null,
              "addAttribute");
        check(c.getDeclaredMethod("addObjectValue", String.class, Class.class, boolean.class, Object.class) != null,
              "addObjectValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIOMetadataFormatImpl fmt = new TestImpl();

        // root and element structure
        check("root".equals(fmt.getRootName()), "getRootName");
        check(fmt.getChildPolicy("root") == IIOMetadataFormat.CHILD_POLICY_SEQUENCE, "root policy SEQUENCE");
        String[] rootKids = fmt.getChildNames("root");
        check(rootKids != null && rootKids.length == 2
              && "item".equals(rootKids[0]) && "meta".equals(rootKids[1]), "root child names item,meta");

        // REPEAT multiplicity
        check(fmt.getChildPolicy("item") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "item policy REPEAT");
        check(fmt.getElementMinChildren("item") == 1, "item min children");
        check(fmt.getElementMaxChildren("item") == 4, "item max children");

        // EMPTY child returns null child names
        check(fmt.getChildPolicy("meta") == IIOMetadataFormat.CHILD_POLICY_EMPTY, "meta policy EMPTY");
        check(fmt.getChildNames("meta") == null, "meta child names null");

        // attributes
        String[] attrs = fmt.getAttributeNames("item");
        check(attrs.length == 3 && contains(attrs, "name") && contains(attrs, "active") && contains(attrs, "hidden"),
              "item attributes");
        check(fmt.getAttributeDataType("item", "name") == IIOMetadataFormat.DATATYPE_STRING, "name datatype");
        check(fmt.isAttributeRequired("item", "name"), "name required");
        check("unnamed".equals(fmt.getAttributeDefaultValue("item", "name")), "name default");
        check(fmt.getAttributeValueType("item", "active") == IIOMetadataFormat.VALUE_ENUMERATION, "active enumeration");
        check(Arrays.equals(fmt.getAttributeEnumerations("item", "active"), new String[]{"TRUE", "FALSE"}), "active enums");
        check("TRUE".equals(fmt.getAttributeDefaultValue("item", "active")), "active default TRUE");
        check(fmt.getAttributeDataType("item", "hidden") == IIOMetadataFormat.DATATYPE_BOOLEAN, "hidden datatype");
        check(fmt.getAttributeDefaultValue("item", "hidden") == null, "hidden no default");

        // object values: arbitrary / enumeration / list
        check(fmt.getObjectValueType("item") == IIOMetadataFormat.VALUE_ARBITRARY, "item object arbitrary");
        check(fmt.getObjectClass("item") == Integer.class, "item object class Integer");
        check(Integer.valueOf(1).equals(fmt.getObjectDefaultValue("item")), "item object default 1");
        check(fmt.getObjectValueType("meta") == IIOMetadataFormat.VALUE_ENUMERATION, "meta object enumeration");
        check(fmt.getObjectClass("meta") == String.class, "meta object class String");
        Object[] enums = fmt.getObjectEnumerations("meta");
        check(enums.length == 2 && "x".equals(enums[0]) && "y".equals(enums[1]), "meta object enumerations");
        check(fmt.getObjectDefaultValue("meta") == null, "meta object default null");
        check(fmt.getObjectValueType("root") == IIOMetadataFormat.VALUE_LIST, "root object list");
        check(fmt.getObjectClass("root") == int.class, "root object class int");
        check(fmt.getObjectArrayMinLength("root") == 0, "root array min");
        check(fmt.getObjectArrayMaxLength("root") == 3, "root array max");

        // description lookup without a resource bundle -> null
        check(fmt.getElementDescription("root", Locale.ROOT) == null, "element description null (no bundle)");
        check(fmt.getAttributeDescription("item", "name", Locale.ROOT) == null, "attribute description null (no bundle)");

        // mutators: removeAttribute / removeElement / addAttribute(list variant)
        TestImpl m = new TestImpl();
        m.removeAttr("item", "name");
        check(!contains(m.getAttributeNames("item"), "name"), "removeAttribute removes name");
        m.addAttrList("item", "tags", IIOMetadataFormat.DATATYPE_INTEGER, false, 2, 6);
        check(m.getAttributeValueType("item", "tags") == IIOMetadataFormat.VALUE_LIST, "addAttribute list");
        check(m.getAttributeListMinLength("item", "tags") == 2, "added list min");
        check(m.getAttributeListMaxLength("item", "tags") == 6, "added list max");
        m.removeElem("meta");
        expectThrows(IllegalArgumentException.class, () -> m.getChildPolicy("meta"), "removeElement removes meta");

        // standard format singleton
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check(std != null, "standard format non-null");
        check("javax_imageio_1.0".equals(std.getRootName()), "standard format root name");
        check(IIOMetadataFormatImpl.getStandardFormatInstance() == std, "standard format singleton");

        // constructor contract violations
        expectThrows(IllegalArgumentException.class, () -> new BadRoot(null, IIOMetadataFormat.CHILD_POLICY_ALL),
                     "ctor null root");
        expectThrows(IllegalArgumentException.class, () -> new BadRoot("r", IIOMetadataFormat.CHILD_POLICY_REPEAT),
                     "ctor REPEAT in (root,policy)");
        expectThrows(IllegalArgumentException.class, () -> new BadRepeat("r", -1, 2), "ctor negative min");
        expectThrows(IllegalArgumentException.class, () -> new BadRepeat("r", 3, 2), "ctor min > max");

        // query contract violations
        expectThrows(IllegalArgumentException.class, () -> fmt.getElementMinChildren("root"),
                     "getElementMinChildren on non-REPEAT");
        expectThrows(IllegalArgumentException.class, () -> fmt.getAttributeEnumerations("item", "name"),
                     "getAttributeEnumerations on non-enumeration");
        expectThrows(IllegalArgumentException.class, () -> fmt.getAttributeDescription("item", "nope", null),
                     "getAttributeDescription unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> fmt.getObjectValueType("absent"),
                     "getObjectValueType unknown element");
    }

    static boolean contains(String[] arr, String s) {
        for (String x : arr) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
