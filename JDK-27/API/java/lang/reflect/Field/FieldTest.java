/*
 * @test
 * @summary java.lang.reflect.Field conformance: white-box structure + black-box behavior.
 * @run main FieldTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int pub;
        private String priv;
        public static final double CONST = 3.14;
    }

    static class Entity {
        public int count = 1;
        public String label = "init";
        public long stamp = 10L;
        public double ratio = 0.5;
        public boolean active = true;
        public char grade = 'A';
        public byte code = 7;
        public short small = 300;
        public float weight = 1.5f;
        public long[] data = {10L, 20L, 30L};
        public static String slogan = "v1";
        public final int CONST = 99;
        public List<String> tags;
        public Map<String, List<Integer>> deep;
        @Deprecated
        public int old;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Field f = Sample.class.getField("pub");
        check("pub".equals(f.getName()), "getName");
        check(f.getType() == int.class, "getType");
        check(f.getGenericType() == int.class, "getGenericType");
        check(f.getModifiers() == Modifier.PUBLIC, "getModifiers");
        check(f.getDeclaringClass() == Sample.class, "getDeclaringClass");
        Sample s = new Sample();
        f.setInt(s, 5);
        check(f.getInt(s) == 5, "setInt/getInt");
        f.set(s, Integer.valueOf(9));
        check(((Integer) f.get(s)).intValue() == 9, "set/get Object");

        Field c = Sample.class.getField("CONST");
        check(Modifier.isStatic(c.getModifiers()), "static");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(((Double) c.get(null)).doubleValue() == 3.14, "get static");

        Field priv = Sample.class.getDeclaredField("priv");
        check(priv.getType() == String.class, "declared field type");
        check(Sample.class.getFields().length == 2, "getFields length");
        check(Sample.class.getDeclaredFields().length == 3, "getDeclaredFields length");
        check(f.toString() != null, "toString");
        check(f.toGenericString() != null, "toGenericString");
        check(Modifier.isFinal(Field.class.getModifiers()), "Field is final");
        boolean implMember = false;
        for (Class<?> i : Field.class.getInterfaces()) if (i.getName().equals("java.lang.reflect.Member")) implMember = true;
        check(implMember, "Field implements Member");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Entity e = new Entity();

        // 1. typed get/set round-trips for every primitive kind + String
        Field count = Entity.class.getField("count");
        count.setInt(e, 41);
        check(count.getInt(e) == 41, "setInt/getInt");
        count.set(e, Integer.valueOf(42));
        check(count.getInt(e) == 42 && ((Integer) count.get(e)) == 42, "set/get boxed int");
        check(count.get(e) instanceof Integer, "int field get boxes to Integer");
        check(count.getLong(e) == 42L, "getLong widening from int field");
        check(count.getDouble(e) == 42.0, "getDouble widening from int field");
        check(Entity.class.getField("code").getByte(e) == (byte) 7, "getByte");
        check(Entity.class.getField("small").getShort(e) == (short) 300, "getShort");
        check(Entity.class.getField("grade").getChar(e) == 'A', "getChar");
        check(Entity.class.getField("weight").getFloat(e) == 1.5f, "getFloat");
        check(Entity.class.getField("stamp").getLong(e) == 10L, "getLong");
        Field ratio = Entity.class.getField("ratio");
        ratio.setDouble(e, 0.25);
        check(ratio.getDouble(e) == 0.25, "setDouble/getDouble");
        check(Entity.class.getField("active").getBoolean(e), "getBoolean");
        Field label = Entity.class.getField("label");
        label.set(e, "changed");
        check("changed".equals(label.get(e)), "set/get String object field");

        // 2. narrowing get must be rejected
        try { count.setLong(e, 1L); check(false, "setLong on int field must fail"); }
        catch (IllegalArgumentException ex) { check(true, "IllegalArgumentException narrowing set"); }
        try { Entity.class.getField("stamp").getInt(e); check(false, "getInt on long field must fail"); }
        catch (IllegalArgumentException ex) { check(true, "IllegalArgumentException narrowing get"); }

        // 3. static field: get(null)/set(null) round-trip
        Field slogan = Entity.class.getField("slogan");
        check("v1".equals(slogan.get(null)), "static get(null)");
        slogan.set(null, "v2");
        check("v2".equals(slogan.get(null)) && "v2".equals(Entity.slogan), "static set(null) + read-back");
        check(Modifier.isStatic(slogan.getModifiers()), "slogan static");

        // 4. final instance field cannot be set
        Field konst = Entity.class.getField("CONST");
        try { konst.set(e, 1); check(false, "final set must fail"); }
        catch (IllegalAccessException ex) { check(true, "IllegalAccessException final set"); }

        // 5. array field: get returns the live array
        Field data = Entity.class.getField("data");
        long[] arr = (long[]) data.get(e);
        arr[1] = 99L;
        check(((long[]) data.get(e))[1] == 99L, "array field mutation visible");
        data.set(e, new long[]{7L});
        check(((long[]) data.get(e)).length == 1 && ((long[]) data.get(e))[0] == 7L,
              "array field replacement");

        // 6. private field in another nest: access control
        Field hidden = FieldSeparate.class.getDeclaredField("hidden");
        try { hidden.get(new FieldSeparate()); check(false, "private get must be denied"); }
        catch (IllegalAccessException ex) { check(true, "IllegalAccessException private get"); }
        hidden.setAccessible(true);
        check("priv".equals(hidden.get(new FieldSeparate())), "private get after setAccessible");
        hidden.setAccessible(true);
        FieldSeparate s2 = new FieldSeparate();
        hidden.set(s2, "wrote");
        check("wrote".equals(hidden.get(s2)), "private set after setAccessible");

        // 7. receiver checks
        try { count.get("not-an-Entity"); check(false, "wrong receiver must fail"); }
        catch (IllegalArgumentException ex) { check(true, "IllegalArgumentException wrong receiver"); }

        // 8. generic field types
        Field tags = Entity.class.getField("tags");
        check(tags.getType() == List.class, "tags erased type");
        ParameterizedType pt = (ParameterizedType) tags.getGenericType();
        check(pt.getRawType() == List.class, "tags raw List");
        check(pt.getActualTypeArguments()[0] == String.class, "tags arg String");
        check(pt.getTypeName().equals("java.util.List<java.lang.String>"), "tags typeName");
        check(tags.getAnnotatedType().getType() instanceof ParameterizedType, "tags annotated type");
        Field deep = Entity.class.getField("deep");
        check(deep.getGenericType().getTypeName()
              .equals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
              "nested generic typeName");

        // 9. annotations and misc
        Field old = Entity.class.getField("old");
        check(old.isAnnotationPresent(Deprecated.class), "isAnnotationPresent on field");
        check(old.getAnnotation(Deprecated.class) != null, "getAnnotation Deprecated");
        check(old.getAnnotations().length == 1, "getAnnotations length");
        check(!count.isSynthetic() && !count.isEnumConstant(), "plain field flags");

        // 10. equality/hashCode and string forms
        Field count2 = Entity.class.getField("count");
        check(count.equals(count2) && count2.equals(count), "Field equals by value");
        check(count.hashCode() == count2.hashCode(), "Field hashCode consistent");
        check(!count.equals(label), "different fields not equal");
        check(count.toString().contains("count"), "toString contains name");
        check(tags.toGenericString().contains("java.util.List<java.lang.String>"),
              "toGenericString shows generic type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// second top-level class = a different nest: its private field is not
// accessible to FieldTest, exercising the access-control paths.
class FieldSeparate {
    private String hidden = "priv";
}
