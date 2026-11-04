/*
 * @test
 * @summary javax.management.openmbean.OpenType conformance: white-box structure + black-box behavior.
 * @run main OpenTypeTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

public class OpenTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Modifier.isProtected(c.getDeclaredConstructor(String.class, String.class, String.class).getModifiers()),
            "protected ctor");
        check(c.getMethod("isValue", Object.class) != null && Modifier.isAbstract(c.getMethod("isValue", Object.class).getModifiers()),
            "abstract isValue");
        check(c.getMethod("equals", Object.class) != null && Modifier.isAbstract(c.getMethod("equals", Object.class).getModifiers()),
            "abstract equals");
        check(c.getMethod("hashCode") != null && Modifier.isAbstract(c.getMethod("hashCode").getModifiers()),
            "abstract hashCode");
        check(c.getMethod("toString") != null && Modifier.isAbstract(c.getMethod("toString").getModifiers()),
            "abstract toString");

        // ALLOWED_CLASSNAMES_LIST contains the 14 simple open-data class names + CompositeData + TabularData
        List<String> allowed = OpenType.ALLOWED_CLASSNAMES_LIST;
        String[] expected = {
            "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte",
            "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
            "java.lang.Double", "java.lang.String", "java.math.BigDecimal", "java.math.BigInteger",
            "java.util.Date", "javax.management.ObjectName",
            CompositeData.class.getName(), TabularData.class.getName()
        };
        for (String e : expected) check(allowed.contains(e), "allowed list contains " + e);
        check(allowed.size() == expected.length, "allowed list size " + expected.length);
        // deprecated ALLOWED_CLASSNAMES array still present (reflectively, avoiding compile-time deprecation)
        check(c.getField("ALLOWED_CLASSNAMES") != null, "ALLOWED_CLASSNAMES field present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // getClassName/getTypeName/getDescription/isArray via concrete subclasses
        OpenType<?> simple = SimpleType.STRING;
        check(simple.getClassName().equals("java.lang.String"), "SimpleType className");
        check(simple.getTypeName().equals("java.lang.String"), "SimpleType typeName");
        check(simple.getDescription().equals("java.lang.String"), "SimpleType description");
        check(!simple.isArray(), "SimpleType not array");

        OpenType<?> array = ArrayType.getArrayType(SimpleType.STRING);
        check(array.getClassName().equals("[Ljava.lang.String;"), "ArrayType className");
        check(array.isArray(), "ArrayType isArray true");

        CompositeType ct = new CompositeType("CT", "d", new String[]{"a"}, new String[]{"da"}, new OpenType[]{SimpleType.STRING});
        check(ct.getClassName().equals(CompositeData.class.getName()), "CompositeType className");
        check(ct.getTypeName().equals("CT"), "CompositeType typeName");
        check(ct.getDescription().equals("d"), "CompositeType description");
        check(!ct.isArray(), "CompositeType not array");

        TabularType tt = new TabularType("TT", "td", ct, new String[]{"a"});
        check(tt.getClassName().equals(TabularData.class.getName()), "TabularType className");
        check(tt.getTypeName().equals("TT"), "TabularType typeName");
        check(!tt.isArray(), "TabularType not array");

        // isValue is abstract and correctly implemented by each concrete subclass
        check(simple.isValue("x"), "SimpleType isValue");
        check(!simple.isValue(5), "SimpleType isValue wrong type");
        check(array.isValue(new String[]{"x"}), "ArrayType isValue");
        check(ct.isValue(null) == false, "CompositeType isValue null false");
        check(tt.isValue(null) == false, "TabularType isValue null false");

        // equals/hashCode/toString contract across subclasses
        check(simple.equals(SimpleType.STRING), "SimpleType equals");
        check(simple.hashCode() == SimpleType.STRING.hashCode(), "SimpleType hashCode");
        check(array.equals(ArrayType.getArrayType(SimpleType.STRING)), "ArrayType equals");
        check(array.hashCode() == ArrayType.getArrayType(SimpleType.STRING).hashCode(), "ArrayType hashCode");
        check(simple.toString().contains("java.lang.String"), "SimpleType toString");
        check(array.toString().contains("ArrayType"), "ArrayType toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
