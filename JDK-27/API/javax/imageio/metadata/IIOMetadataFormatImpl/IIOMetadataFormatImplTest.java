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

    // ---- Part 3: resource bundle contract (probed on jdk-27.0.1) ----
    static class BundleImpl extends IIOMetadataFormatImpl {
        BundleImpl() { super("root", CHILD_POLICY_EMPTY); }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
        String rbn() { return getResourceBaseName(); }
        void setRbn(String s) { setResourceBaseName(s); }
    }
    static void testResourceBundle() throws Exception {
        BundleImpl t = new BundleImpl();
        check(t.rbn().equals(IIOMetadataFormatImplTest.class.getName() + "$BundleImplResources"),
              "default resource base name is subclass name + Resources");
        expectThrows(IllegalArgumentException.class, () -> t.setRbn(null), "setResourceBaseName null");
        t.setRbn("my.custom.Bundle");
        check("my.custom.Bundle".equals(t.rbn()), "resource base name round-trip");
        check(t.getElementDescription("root", Locale.ROOT) == null, "missing bundle description null");
        check(t.getElementDescription("root", null) == null, "null locale accepted, description null");
        expectThrows(IllegalArgumentException.class, () -> t.getElementDescription("nope", null),
                     "getElementDescription unknown element");
        // standard format descriptions come from the JDK-provided bundle
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check(std.getElementDescription("Chroma", Locale.ROOT) != null, "standard Chroma description non-null");
        check(std.getElementDescription("javax_imageio_1.0", Locale.ROOT) == null, "standard root description null");
    }

    // ---- Part 4: object value range variant + VALUE_NONE family ----
    static class ObjImpl extends IIOMetadataFormatImpl {
        ObjImpl() { super("r", CHILD_POLICY_EMPTY); }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
        void addRange() { addObjectValue("r", Integer.class, Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(9), true, false); }
        void rm() { removeObjectValue("r"); }
    }
    static void testObjectValuesDeep() throws Exception {
        ObjImpl o = new ObjImpl();
        o.addRange();
        check(o.getObjectValueType("r") == IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE,
              "range object VALUE_RANGE_MIN_INCLUSIVE");
        check(Integer.valueOf(0).compareTo((Integer) o.getObjectMinValue("r")) == 0, "range object min");
        check(Integer.valueOf(9).compareTo((Integer) o.getObjectMaxValue("r")) == 0, "range object max");
        o.rm();
        check(o.getObjectValueType("r") == IIOMetadataFormat.VALUE_NONE, "VALUE_NONE after removeObjectValue");
        expectThrows(IllegalArgumentException.class, () -> o.getObjectDefaultValue("r"),
                     "getObjectDefaultValue after remove");
        expectThrows(IllegalArgumentException.class, () -> o.getObjectClass("r"), "getObjectClass after remove");
        expectThrows(IllegalArgumentException.class, () -> o.getObjectEnumerations("r"),
                     "getObjectEnumerations after remove");
        expectThrows(IllegalArgumentException.class, () -> o.getObjectMinValue("r"),
                     "getObjectMinValue after remove");
        expectThrows(IllegalArgumentException.class, () -> o.getObjectArrayMinLength("r"),
                     "getObjectArrayMinLength after remove");
    }

    // ---- Part 5: builder/ctor negative matrix + probed quirks ----
    static class NegImpl extends IIOMetadataFormatImpl {
        NegImpl() { super("root", CHILD_POLICY_SEQUENCE); addElement("item", "root", 1, 4); }
        NegImpl(String r, int p) { super(r, p); }
        NegImpl(String r, int mn, int mx) { super(r, mn, mx); }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
        void addEl3(String c, String p, int pol) { addElement(c, p, pol); }
        void addEl4(String c, String p, int mn, int mx) { addElement(c, p, mn, mx); }
        void addAttrDef(String e, String a, int t, boolean req, String d) { addAttribute(e, a, t, req, d); }
        void addAttrEnum(String e, String a, int t, boolean req, String d, java.util.List<String> en) { addAttribute(e, a, t, req, d, en); }
        void addAttrList(String e, String a, int t, boolean req, int mn, int mx) { addAttribute(e, a, t, req, mn, mx); }
        void addChild(String c, String p) { addChildElement(c, p); }
    }
    static void testNegativesDeep() throws Exception {
        NegImpl t = new NegImpl();
        expectThrows(IllegalArgumentException.class, () -> t.addEl3("z", "noParent", 0), "addElement unknown parent");
        expectThrows(IllegalArgumentException.class, () -> t.addEl3("z", "root", 99), "addElement invalid policy");
        expectThrows(IllegalArgumentException.class,
                     () -> t.addEl3("z", "root", IIOMetadataFormat.CHILD_POLICY_REPEAT),
                     "addElement REPEAT not allowed in (String,String,int)");
        expectThrows(IllegalArgumentException.class, () -> t.addEl4("z", "root", 3, 2), "addElement min > max");
        expectThrows(IllegalArgumentException.class, () -> t.addEl4("z", "root", -1, 2), "addElement negative min");
        expectThrows(IllegalArgumentException.class, () -> t.addAttrDef("noelem", "a", 0, true, "d"),
                     "addAttribute unknown element");
        expectThrows(IllegalArgumentException.class, () -> t.addAttrDef("item", "a", 99, true, "d"),
                     "addAttribute invalid dataType");
        expectThrows(IllegalArgumentException.class, () -> t.addAttrEnum("item", "a", 0, true, "d", null),
                     "addAttribute enumeration null list");
        expectThrows(IllegalArgumentException.class,
                     () -> t.addAttrEnum("item", "a", 0, true, "d", java.util.List.<String>of()),
                     "addAttribute enumeration empty list");
        expectThrows(IllegalArgumentException.class,
                     () -> t.addAttrList("item", "l2", IIOMetadataFormat.DATATYPE_INTEGER, false, 5, 2),
                     "addAttribute list min > max");
        expectThrows(IllegalArgumentException.class, () -> t.addChild("z", "noParent"),
                     "addChildElement unknown parent");
        // ctor validation
        expectThrows(IllegalArgumentException.class, () -> new NegImpl("r", 6), "ctor policy 6");
        expectThrows(IllegalArgumentException.class, () -> new NegImpl("r", -1), "ctor policy -1");
        expectThrows(IllegalArgumentException.class, () -> new NegImpl("r", IIOMetadataFormat.CHILD_POLICY_REPEAT),
                     "ctor REPEAT in (String,int)");
        for (int p = 0; p <= 4; p++) {
            final int pol = p;
            NegImpl v = new NegImpl("p" + pol, pol);
            check(v.getChildPolicy("p" + pol) == pol, "policy " + pol + " accepted by 2-arg ctor");
        }
        NegImpl rep = new NegImpl("r", 2, 5);
        check(rep.getChildPolicy("r") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "3-arg ctor forces REPEAT");
        check(rep.getElementMinChildren("r") == 2 && rep.getElementMaxChildren("r") == 5, "3-arg ctor bounds");
        // query-time negatives
        NegImpl q = new NegImpl();
        expectThrows(IllegalArgumentException.class, () -> q.getElementMaxChildren("root"),
                     "getElementMaxChildren non-REPEAT");
        expectThrows(IllegalArgumentException.class, () -> q.getAttributeMinValue("item", "noattr"),
                     "getAttributeMinValue unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> q.getAttributeMaxValue("item", "noattr"),
                     "getAttributeMaxValue unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> q.getAttributeListMinLength("item", "noattr"),
                     "getAttributeListMinLength unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> q.isAttributeRequired("item", "noattr"),
                     "isAttributeRequired unknown attribute");
        expectThrows(IllegalArgumentException.class, () -> q.getAttributeNames("absent"),
                     "getAttributeNames unknown element");
        expectThrows(IllegalArgumentException.class, () -> q.getElementDescription("absent", null),
                     "getElementDescription unknown element");
        expectThrows(IllegalArgumentException.class, () -> q.getChildPolicy(null), "getChildPolicy null");
        // duplicate addAttribute: default overwritten, names list grows (probed quirk)
        NegImpl dup = new NegImpl();
        dup.addAttrDef("item", "dupattr", IIOMetadataFormat.DATATYPE_STRING, true, "first");
        dup.addAttrDef("item", "dupattr", IIOMetadataFormat.DATATYPE_STRING, true, "second");
        check("second".equals(dup.getAttributeDefaultValue("item", "dupattr")), "duplicate addAttribute default overwritten");
        check(dup.getAttributeNames("item").length == 2, "duplicate addAttribute grows the names list");
        // element without attributes yields an empty array
        NegImpl empty = new NegImpl("eroot", IIOMetadataFormat.CHILD_POLICY_EMPTY);
        check(empty.getAttributeNames("eroot").length == 0, "childless element has empty attribute array");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testResourceBundle();
        testObjectValuesDeep();
        testNegativesDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
