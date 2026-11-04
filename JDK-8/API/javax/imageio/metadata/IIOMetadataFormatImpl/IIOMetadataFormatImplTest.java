/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataFormatImpl conformance: abstract hierarchy and methods.
 * @run main IIOMetadataFormatImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class IIOMetadataFormatImplTest {
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

    /** Rich concrete subclass exercising the protected builder methods. */
    static class TestImpl extends IIOMetadataFormatImpl {
        TestImpl() { this("root", CHILD_POLICY_SEQUENCE); build(); }
        TestImpl(String r, int policy) { super(r, policy); }
        TestImpl(String r, int min, int max) { super(r, min, max); }
        void build() {
            addElement("item", "root", 1, 4);
            addElement("meta", "item", CHILD_POLICY_EMPTY);
            addChildElement("meta", "root");
            addAttribute("item", "name", DATATYPE_STRING, true, "unnamed");
            addBooleanAttribute("item", "active", true, true);
            addBooleanAttribute("item", "hidden", false, false);
            addObjectValue("item", Integer.class, true, Integer.valueOf(1));
            addObjectValue("meta", String.class, false, null, Arrays.asList("x", "y"));
            addObjectValue("root", int.class, 0, 3);
        }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
        // protected surface exposed for probing
        String rbn() { return getResourceBaseName(); }
        void setRbn(String s) { setResourceBaseName(s); }
        void addEl3(String c, String p, int pol) { addElement(c, p, pol); }
        void addEl4(String c, String p, int mn, int mx) { addElement(c, p, mn, mx); }
        void addAttrDef(String e, String a, int t, boolean req, String d) { addAttribute(e, a, t, req, d); }
        void addAttrEnum(String e, String a, int t, boolean req, String d, List<String> en) { addAttribute(e, a, t, req, d, en); }
        void addAttrRange(String e, String a, int t, boolean req, String d, String mn, String mx, boolean mni, boolean mxi) { addAttribute(e, a, t, req, d, mn, mx, mni, mxi); }
        void addAttrList(String e, String a, int t, boolean req, int mn, int mx) { addAttribute(e, a, t, req, mn, mx); }
        void addChild(String c, String p) { addChildElement(c, p); }
        void rmElem(String e) { removeElement(e); }
        void rmAttr(String e, String a) { removeAttribute(e, a); }
        void rmObj(String e) { removeObjectValue(e); }
        void addObjRange(String e, Integer d, Integer mn, Integer mx, boolean mni, boolean mxi) {
            addObjectValue(e, Integer.class, d, mn, mx, mni, mxi);
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormatImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.metadata.IIOMetadataFormat"), "implements javax.imageio.metadata.IIOMetadataFormat");
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
        check(dm.contains("getStandardFormatInstance"), "method getStandardFormatInstance");
        // protected builder surface
        check(dm.contains("addElement"), "protected addElement");
        check(dm.contains("addChildElement"), "protected addChildElement");
        check(dm.contains("removeElement"), "protected removeElement");
        check(dm.contains("addAttribute"), "protected addAttribute");
        check(dm.contains("addBooleanAttribute"), "protected addBooleanAttribute");
        check(dm.contains("removeAttribute"), "protected removeAttribute");
        check(dm.contains("addObjectValue"), "protected addObjectValue");
        check(dm.contains("removeObjectValue"), "protected removeObjectValue");
        check(dm.contains("setResourceBaseName"), "protected setResourceBaseName");
        check(dm.contains("getResourceBaseName"), "protected getResourceBaseName");
        testStructural();
        testBehavior();
        testObjectValues();
        testResourceBundle();
        testStandardInstance();
        testBuilderNegatives();
        testCtorNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean contains(String[] arr, String s) {
        for (String x : arr) if (s.equals(x)) return true;
        return false;
    }

    static void testStructural() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormatImpl");
        Field nameField = c.getField("standardMetadataFormatName");
        check(Modifier.isPublic(nameField.getModifiers()) && Modifier.isStatic(nameField.getModifiers())
              && Modifier.isFinal(nameField.getModifiers()), "standardMetadataFormatName public static final");
        check(nameField.getType() == String.class, "standardMetadataFormatName is String");
        check("javax_imageio_1.0".equals(nameField.get(null)), "standardMetadataFormatName value");
        Method factory = c.getMethod("getStandardFormatInstance");
        check(Modifier.isPublic(factory.getModifiers()) && Modifier.isStatic(factory.getModifiers()),
              "getStandardFormatInstance public static");
        check(factory.getReturnType().getName().equals("javax.imageio.metadata.IIOMetadataFormat"),
              "getStandardFormatInstance returns IIOMetadataFormat");
        check(c.getDeclaredConstructor(String.class, int.class) != null, "ctor (String,int)");
        check(c.getDeclaredConstructor(String.class, int.class, int.class) != null, "ctor (String,int,int)");
        check(Modifier.isPublic(c.getDeclaredConstructor(String.class, int.class).getModifiers()),
              "ctor (String,int) public");
        check(Modifier.isPublic(c.getDeclaredConstructor(String.class, int.class, int.class).getModifiers()),
              "ctor (String,int,int) public");
    }

    static void testBehavior() throws Exception {
        IIOMetadataFormatImpl fmt = new TestImpl();
        check("root".equals(fmt.getRootName()), "getRootName");
        check(fmt.getChildPolicy("root") == IIOMetadataFormat.CHILD_POLICY_SEQUENCE, "root policy SEQUENCE");
        String[] rootKids = fmt.getChildNames("root");
        check(rootKids != null && rootKids.length == 2
              && "item".equals(rootKids[0]) && "meta".equals(rootKids[1]), "root child names item,meta");
        check(fmt.getChildPolicy("item") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "item policy REPEAT");
        check(fmt.getElementMinChildren("item") == 1, "item min children");
        check(fmt.getElementMaxChildren("item") == 4, "item max children");
        check(fmt.getChildPolicy("meta") == IIOMetadataFormat.CHILD_POLICY_EMPTY, "meta policy EMPTY");
        check(fmt.getChildNames("meta") == null, "meta child names null for EMPTY");
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
        check(fmt.isAttributeRequired("item", "hidden"), "boolean attributes are always required");
        check(fmt.canNodeAppear("item", null), "canNodeAppear via subclass");
        check(fmt.getElementDescription("root", Locale.ROOT) == null, "description null without bundle");

        // mutators: removeAttribute / removeElement / addAttribute list variant
        TestImpl m = new TestImpl();
        m.rmAttr("item", "name");
        check(!contains(m.getAttributeNames("item"), "name"), "removeAttribute removes name");
        m.addAttrList("item", "tags", IIOMetadataFormat.DATATYPE_INTEGER, false, 2, 6);
        check(m.getAttributeValueType("item", "tags") == IIOMetadataFormat.VALUE_LIST, "addAttribute list");
        check(m.getAttributeListMinLength("item", "tags") == 2, "added list min");
        check(m.getAttributeListMaxLength("item", "tags") == 6, "added list max");
        m.rmElem("meta");
        expectThrows("java.lang.IllegalArgumentException", () -> m.getChildPolicy("meta"), "removeElement removes meta");
        m.rmAttr("item", "absent");
        check(m.getAttributeNames("item").length == 3, "removeAttribute of absent attr is a no-op");
        m.rmElem("noSuchElement");
        check(m.getChildPolicy("root") == IIOMetadataFormat.CHILD_POLICY_SEQUENCE, "removeElement of absent element no-op");
    }

    static void testObjectValues() throws Exception {
        IIOMetadataFormatImpl fmt = new TestImpl();
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
        // Comparable range variant
        TestImpl r = new TestImpl();
        r.addObjRange("root", Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(9), true, false);
        check(r.getObjectValueType("root") == IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE,
              "range object VALUE_RANGE_MIN_INCLUSIVE");
        check(Integer.valueOf(0).compareTo((Integer) r.getObjectMinValue("root")) == 0, "range object min");
        check(Integer.valueOf(9).compareTo((Integer) r.getObjectMaxValue("root")) == 0, "range object max");
        // removeObjectValue returns element to VALUE_NONE; other object getters then throw
        r.rmObj("root");
        check(r.getObjectValueType("root") == IIOMetadataFormat.VALUE_NONE, "VALUE_NONE after removeObjectValue");
        expectThrows("java.lang.IllegalArgumentException", () -> r.getObjectDefaultValue("root"),
                     "getObjectDefaultValue after remove");
        expectThrows("java.lang.IllegalArgumentException", () -> r.getObjectClass("root"),
                     "getObjectClass after remove");
        expectThrows("java.lang.IllegalArgumentException", () -> r.getObjectEnumerations("root"),
                     "getObjectEnumerations after remove");
        expectThrows("java.lang.IllegalArgumentException", () -> r.getObjectMinValue("root"),
                     "getObjectMinValue after remove");
        expectThrows("java.lang.IllegalArgumentException", () -> r.getObjectArrayMinLength("root"),
                     "getObjectArrayMinLength after remove");
    }

    static void testResourceBundle() throws Exception {
        TestImpl t = new TestImpl();
        check(t.rbn().equals(IIOMetadataFormatImplTest.class.getName() + "$TestImplResources"),
              "default resource base name is subclass name + Resources");
        expectThrows("java.lang.IllegalArgumentException", () -> t.setRbn(null), "setResourceBaseName null");
        t.setRbn("my.custom.Bundle");
        check("my.custom.Bundle".equals(t.rbn()), "resource base name round-trip");
        check(t.getElementDescription("root", Locale.ROOT) == null, "missing bundle description null");
        check(t.getElementDescription("root", null) == null, "null locale accepted, description null");
        expectThrows("java.lang.IllegalArgumentException", () -> t.getElementDescription("nope", null),
                     "getElementDescription unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> t.getAttributeDescription("item", "nope", null),
                     "getAttributeDescription unknown attribute");
    }

    static void testStandardInstance() throws Exception {
        IIOMetadataFormat std = IIOMetadataFormatImpl.getStandardFormatInstance();
        check(std != null, "standard format non-null");
        check("javax_imageio_1.0".equals(std.getRootName()), "standard format root name");
        check(IIOMetadataFormatImpl.getStandardFormatInstance() == std, "standard format singleton identity");
        check(std instanceof IIOMetadataFormat, "standard format implements the interface");
        check(std.getChildPolicy("javax_imageio_1.0") == IIOMetadataFormat.CHILD_POLICY_SOME, "root policy SOME");
        check(std.getElementDescription("Chroma", Locale.ROOT) != null,
              "standard descriptions come from the JDK bundle");
        check(std.getElementDescription("javax_imageio_1.0", Locale.ROOT) == null, "root description absent");
    }

    static void testBuilderNegatives() throws Exception {
        TestImpl t = new TestImpl();
        expectThrows("java.lang.IllegalArgumentException", () -> t.addEl3("z", "noParent", 0),
                     "addElement unknown parent");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addEl3("z", "root", 99),
                     "addElement invalid policy");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addEl3("z", "root", IIOMetadataFormat.CHILD_POLICY_REPEAT),
                     "addElement REPEAT not allowed in (String,String,int)");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addEl4("z", "root", 3, 2),
                     "addElement min > max");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addEl4("z", "root", -1, 2),
                     "addElement negative min");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addAttrDef("noelem", "a", 0, true, "d"),
                     "addAttribute unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addAttrDef("item", "a", 99, true, "d"),
                     "addAttribute invalid dataType");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addAttrEnum("item", "a", 0, true, "d", null),
                     "addAttribute enumeration null list");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addAttrEnum("item", "a", 0, true, "d", new ArrayList<String>()),
                     "addAttribute enumeration empty list");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> t.addAttrList("item", "l2", IIOMetadataFormat.DATATYPE_INTEGER, false, 5, 2),
                     "addAttribute list min > max");
        expectThrows("java.lang.IllegalArgumentException", () -> t.addChild("z", "noParent"),
                     "addChildElement unknown parent");
        // addAttribute with same name twice: second registration wins for defaults, names list grows (probed)
        TestImpl dup = new TestImpl();
        dup.addAttrDef("item", "dupattr", 0, true, "first");
        dup.addAttrDef("item", "dupattr", 0, true, "second");
        check("second".equals(dup.getAttributeDefaultValue("item", "dupattr")), "duplicate addAttribute default overwritten");
        check(dup.getAttributeNames("item").length == 5, "duplicate addAttribute grows the names list");
        // query-time negatives (exact messages probed on jdk8u372-b07)
        TestImpl q = new TestImpl();
        expectThrows("java.lang.IllegalArgumentException", () -> q.getElementMinChildren("root"),
                     "getElementMinChildren non-REPEAT");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getElementMaxChildren("root"),
                     "getElementMaxChildren non-REPEAT");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeEnumerations("item", "name"),
                     "getAttributeEnumerations non-enumeration");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeMinValue("item", "name"),
                     "getAttributeMinValue non-range");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeMaxValue("item", "name"),
                     "getAttributeMaxValue non-range");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeListMinLength("item", "name"),
                     "getAttributeListMinLength non-list");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeDescription("item", "nope", null),
                     "getAttributeDescription unknown attribute");
        expectThrows("java.lang.IllegalArgumentException", () -> q.isAttributeRequired("item", "nope"),
                     "isAttributeRequired unknown attribute");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getObjectValueType("absent"),
                     "getObjectValueType unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getChildPolicy(null),
                     "getChildPolicy null element");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getAttributeNames("absent"),
                     "getAttributeNames unknown element");
        expectThrows("java.lang.IllegalArgumentException", () -> q.getElementDescription("absent", null),
                     "getElementDescription unknown element");
        // element with no attributes yields an empty array, not null
        check(q.getAttributeNames("meta").length == 0, "element without attributes gives empty array");
    }

    static void testCtorNegatives() throws Exception {
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl(null, IIOMetadataFormat.CHILD_POLICY_ALL), "ctor null root");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl("r", 6), "ctor policy 6");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl("r", -1), "ctor policy -1");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl("r", IIOMetadataFormat.CHILD_POLICY_REPEAT),
                     "ctor REPEAT in (String,int)");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl("r", -1, 2), "ctor negative min");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new TestImpl("r", 3, 2), "ctor min > max");
        TestImpl rep = new TestImpl("r", 2, 5);
        check(rep.getChildPolicy("r") == IIOMetadataFormat.CHILD_POLICY_REPEAT, "3-arg ctor forces REPEAT");
        check(rep.getElementMinChildren("r") == 2, "3-arg ctor min");
        check(rep.getElementMaxChildren("r") == 5, "3-arg ctor max");
        check("r".equals(rep.getRootName()), "3-arg ctor root name");
        // valid policies 0..4 (not REPEAT=5) accepted by the 2-arg ctor
        for (int p = 0; p <= 4; p++) {
            final int pol = p;
            TestImpl v = new TestImpl("p" + pol, pol);
            check(v.getChildPolicy("p" + pol) == pol, "policy " + pol + " accepted");
        }
    }
}
