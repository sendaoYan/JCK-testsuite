/*
 * @test
 * @summary java.lang.reflect.Field conformance: full contract coverage.
 *          type/name/declaring-class/modifiers, all 8 primitive get/set round-trips,
 *          static vs instance null semantics (get(null) static OK / instance NPE),
 *          wrong-object-type -> IllegalArgumentException, private access without
 *          setAccessible -> IllegalAccessException and success after setAccessible(true),
 *          wrong primitive getter/setter -> IllegalArgumentException, final-field set ->
 *          IllegalAccessException, equals/hashCode/toString/toGenericString,
 *          isEnumConstant/isSynthetic (incl. inner-class synthetic this$0),
 *          getAnnotation/getAnnotationsByType/getDeclaredAnnotations/getAnnotatedType,
 *          and getFields/getDeclaredFields inheritance vs declared-only semantics.
 * @run main FieldTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tag { String value(); }

    static class Base {
        public int inherited = 1;
        private String basePriv = "bp";
    }

    static class Sample extends Base {
        public int pub;
        private String priv;
        public static final double CONST = 3.14;
        public volatile long vl;
        private static int staticPriv;
        public transient Object trans;
        @Tag("x") public String annotated;

        public boolean boolF;
        public byte byteF;
        public char charF;
        public short shortF;
        public float floatF;
    }

    enum Color { RED, GREEN }

    static class Outer {
        class Inner { }
    }

    static void testMetadata() throws Exception {
        Field f = Sample.class.getField("pub");
        check("pub".equals(f.getName()), "getName");
        check(f.getType() == int.class, "getType");
        check(f.getGenericType() == int.class, "getGenericType primitive");
        check(f.getModifiers() == Modifier.PUBLIC, "getModifiers public");
        check(f.getDeclaringClass() == Sample.class, "getDeclaringClass");

        Field c = Sample.class.getField("CONST");
        check(Modifier.isStatic(c.getModifiers()), "CONST static");
        check(Modifier.isFinal(c.getModifiers()), "CONST final");
        check(Modifier.isPublic(c.getModifiers()), "CONST public");

        Field vl = Sample.class.getField("vl");
        check(Modifier.isVolatile(vl.getModifiers()), "vl volatile");
        check(Modifier.isPublic(vl.getModifiers()), "vl public");

        Field tr = Sample.class.getField("trans");
        check(Modifier.isTransient(tr.getModifiers()), "trans transient");

        // generic type for an Object field is Object.class
        check(tr.getGenericType() == Object.class, "getGenericType Object");
    }

    static void testPrimitiveRoundTrip() throws Exception {
        Sample s = new Sample();

        Field bf = Sample.class.getField("boolF");
        bf.setBoolean(s, true);
        check(bf.getBoolean(s), "boolean get/set");
        bf.set(s, Boolean.FALSE);
        check(!bf.getBoolean(s), "boolean boxed set/get");

        Field byf = Sample.class.getField("byteF");
        byf.setByte(s, (byte) -9);
        check(byf.getByte(s) == (byte) -9, "byte get/set");
        byf.set(s, Byte.valueOf((byte) 100));
        check(byf.getByte(s) == (byte) 100, "byte boxed");

        Field cf = Sample.class.getField("charF");
        cf.setChar(s, '€');
        check(cf.getChar(s) == '€', "char get/set");
        cf.set(s, Character.valueOf('z'));
        check(cf.getChar(s) == 'z', "char boxed");

        Field sf = Sample.class.getField("shortF");
        sf.setShort(s, (short) 32000);
        check(sf.getShort(s) == (short) 32000, "short get/set");

        Field intf = Sample.class.getField("pub");
        intf.setInt(s, Integer.MAX_VALUE);
        check(intf.getInt(s) == Integer.MAX_VALUE, "int get/set max");
        intf.setInt(s, Integer.MIN_VALUE);
        check(intf.getInt(s) == Integer.MIN_VALUE, "int get/set min");
        intf.set(s, Integer.valueOf(7));
        check(intf.getInt(s) == 7, "int boxed");

        Field lf = Sample.class.getField("vl");
        lf.setLong(s, Long.MAX_VALUE);
        check(lf.getLong(s) == Long.MAX_VALUE, "long get/set max");

        Field ff = Sample.class.getField("floatF");
        ff.setFloat(s, Float.NaN);
        check(Float.isNaN(ff.getFloat(s)), "float NaN");
        ff.setFloat(s, Float.POSITIVE_INFINITY);
        check(Float.isInfinite(ff.getFloat(s)), "float +Inf");

        Field df = Sample.class.getField("CONST");
        // read double static
        check(df.getDouble(null) == 3.14, "double static get null");
        check(df.get(null).equals(Double.valueOf(3.14)), "double get Object");

        // get(Object) on int returns Integer boxed
        check(intf.get(s).equals(Integer.valueOf(7)), "get returns boxed Integer");
    }

    static void testStaticVsInstanceNull() throws Exception {
        Field f = Sample.class.getField("pub");
        checkThrows(NullPointerException.class, () -> f.get(null),
                "instance field get(null) NPE");
        checkThrows(NullPointerException.class, () -> f.getInt(null),
                "instance field getInt(null) NPE");
        checkThrows(NullPointerException.class, () -> f.set(null, Integer.valueOf(1)),
                "instance field set(null,..) NPE");

        Field c = Sample.class.getField("CONST");
        check(c.get(null) != null, "static field get(null) OK");
        check(c.getDouble(null) == 3.14, "static getDouble(null)");

        Field sp = Sample.class.getDeclaredField("staticPriv");
        sp.setAccessible(true);
        sp.setInt(null, 123);
        check(sp.getInt(null) == 123, "static private set/get null after accessible");
    }

    static void testWrongObjectType() throws Exception {
        Field f = Sample.class.getField("pub");
        checkThrows(IllegalArgumentException.class,
                () -> f.get(new Object()), "get wrong object type IAE");
        checkThrows(IllegalArgumentException.class,
                () -> f.getInt("a string"), "getInt wrong object type IAE");
        checkThrows(IllegalArgumentException.class,
                () -> f.set(new Object(), Integer.valueOf(1)), "set wrong object type IAE");
    }

    static void testAccessibility() throws Exception {
        // Use a separate top-level class (not a nestmate): JDK-11+ permits reflection
        // access to a nestmate's private member without setAccessible, so the IAE path
        // must target a non-nest class to remain a valid contract assertion.
        Field priv = ExtField.class.getDeclaredField("hidden");
        check(Modifier.isPrivate(priv.getModifiers()), "priv is private");
        ExtField ef = new ExtField();
        checkThrows(IllegalAccessException.class, () -> priv.get(ef),
                "private get without accessible IAE");
        checkThrows(IllegalAccessException.class, () -> priv.set(ef, "x"),
                "private set without accessible IAE");

        priv.setAccessible(true);
        priv.set(ef, "hello");
        check("hello".equals(priv.get(ef)), "private set/get after setAccessible");
    }

    static void testWrongPrimitiveAccessor() throws Exception {
        Sample s = new Sample();
        Field intf = Sample.class.getField("pub");
        checkThrows(IllegalArgumentException.class,
                () -> intf.getBoolean(s), "getBoolean on int field IAE");
        checkThrows(IllegalArgumentException.class,
                () -> intf.getByte(s), "getByte on int field IAE");
        checkThrows(IllegalArgumentException.class,
                () -> intf.setLong(s, 1L), "setLong on int field IAE");

        Field bf = Sample.class.getField("boolF");
        checkThrows(IllegalArgumentException.class,
                () -> bf.getInt(s), "getInt on boolean field IAE");

        // set(Object) with wrong wrapper type -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> intf.set(s, Long.valueOf(1L)), "set int field with Long IAE");
        checkThrows(IllegalArgumentException.class,
                () -> intf.set(s, "str"), "set int field with String IAE");
        checkThrows(IllegalArgumentException.class,
                () -> intf.set(s, null), "set int field with null IAE");
    }

    static void testFinalFieldSet() throws Exception {
        Field c = Sample.class.getField("CONST");
        checkThrows(IllegalAccessException.class,
                () -> c.set(null, Double.valueOf(2.71)), "set final field IAE");
        checkThrows(IllegalAccessException.class,
                () -> c.setDouble(null, 2.71), "setDouble final field IAE");
    }

    static void testEqualsHashCodeToString() throws Exception {
        Field a = Sample.class.getField("pub");
        Field b = Sample.class.getField("pub");
        Field c = Sample.class.getField("CONST");
        check(a.equals(b), "equals same field");
        check(a.hashCode() == b.hashCode(), "hashCode same field");
        check(!a.equals(c), "equals different field");
        check(!a.equals(null), "equals null");
        check(!a.equals("pub"), "equals non-Field");

        check(a.toString().contains("pub"), "toString contains name");
        check(c.toGenericString().contains("double"), "toGenericString contains type");
        check(a.toString() != null && a.toGenericString() != null, "toString non-null");
    }

    static void testEnumConstantAndSynthetic() throws Exception {
        Field red = Color.class.getField("RED");
        check(red.isEnumConstant(), "enum constant RED isEnumConstant");
        check(Modifier.isStatic(red.getModifiers()), "enum constant static");
        check(Modifier.isFinal(red.getModifiers()), "enum constant final");
        check(red.getType() == Color.class, "enum constant type");
        check(red.get(null) == Color.RED, "enum constant get(null)");

        Field pub = Sample.class.getField("pub");
        check(!pub.isEnumConstant(), "regular field not enum constant");
        check(!pub.isSynthetic(), "regular field not synthetic");

        // enum classes carry a synthetic $VALUES field (private static final)
        boolean foundSynthetic = false;
        for (Field f : Color.class.getDeclaredFields()) {
            if (f.isSynthetic()) {
                foundSynthetic = true;
                check(f.getName().equals("$VALUES"), "synthetic $VALUES name");
                check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                        "synthetic $VALUES static final");
            }
        }
        check(foundSynthetic, "enum has synthetic $VALUES field");

        // JDK-11+ nestmates removed the synthetic this$0 field from inner classes
        boolean foundThis0 = false;
        for (Field f : Outer.Inner.class.getDeclaredFields()) {
            if (f.getName().contains("this$0")) foundThis0 = true;
        }
        check(!foundThis0, "inner class no synthetic this$0 (nestmates)");
        check(Outer.Inner.class.getNestHost() == FieldTest.class,
                "inner class nest host is top-level FieldTest");
    }

    static void testAccessFlags() throws Exception {
        java.util.Set<AccessFlag> pub = Sample.class.getField("pub").accessFlags();
        check(pub.contains(AccessFlag.PUBLIC), "accessFlags PUBLIC for pub");
        java.util.Set<AccessFlag> priv = ExtField.class.getDeclaredField("hidden").accessFlags();
        check(priv.contains(AccessFlag.PRIVATE), "accessFlags PRIVATE for hidden");
        java.util.Set<AccessFlag> c = Sample.class.getField("CONST").accessFlags();
        check(c.contains(AccessFlag.STATIC) && c.contains(AccessFlag.FINAL),
                "accessFlags STATIC+FINAL for CONST");
    }

    static void testAnnotations() throws Exception {
        Field af = Sample.class.getField("annotated");
        Tag tag = af.getAnnotation(Tag.class);
        check(tag != null && "x".equals(tag.value()), "getAnnotation");
        Tag[] tags = af.getAnnotationsByType(Tag.class);
        check(tags.length == 1 && "x".equals(tags[0].value()), "getAnnotationsByType");

        Annotation[] declared = af.getDeclaredAnnotations();
        boolean found = false;
        for (Annotation a : declared) {
            if (a instanceof Tag) found = true;
        }
        check(found, "getDeclaredAnnotations includes Tag");
        check(af.getAnnotatedType() != null, "getAnnotatedType non-null");
        check(af.getAnnotatedType().getType() == String.class, "getAnnotatedType type");

        // un-annotated field has no Tag
        check(Sample.class.getField("pub").getAnnotation(Tag.class) == null,
                "unannotated getAnnotation null");
    }

    static void testFieldDiscovery() throws Exception {
        // getFields returns public fields including inherited; getDeclaredFields only declared
        Field[] pubFields = Sample.class.getFields();
        boolean hasInherited = false, hasPub = false, hasConst = false;
        for (Field f : pubFields) {
            if (f.getName().equals("inherited")) hasInherited = true;
            if (f.getName().equals("pub")) hasPub = true;
            if (f.getName().equals("CONST")) hasConst = true;
            check(Modifier.isPublic(f.getModifiers()), "getFields all public: " + f.getName());
        }
        check(hasInherited, "getFields includes inherited public field");
        check(hasPub && hasConst, "getFields includes declared public fields");

        Field[] declared = Sample.class.getDeclaredFields();
        boolean hasPriv = false, hasInheritedDecl = false;
        for (Field f : declared) {
            if (f.getName().equals("priv")) hasPriv = true;
            if (f.getName().equals("inherited")) hasInheritedDecl = true;
        }
        check(hasPriv, "getDeclaredFields includes private");
        check(!hasInheritedDecl, "getDeclaredFields excludes inherited");

        checkThrows(NoSuchFieldException.class,
                () -> Sample.class.getField("nonexistent"), "getField missing NSEE");
        checkThrows(NoSuchFieldException.class,
                () -> Sample.class.getField("priv"), "getField on private NSEE");
        checkThrows(NoSuchFieldException.class,
                () -> Sample.class.getDeclaredField("nonexistent"),
                "getDeclaredField missing NSEE");
        check(Sample.class.getDeclaredField("priv") != null,
                "getDeclaredField finds private");
        checkThrows(NullPointerException.class,
                () -> Sample.class.getField(null), "getField(null) NPE");
        checkThrows(NullPointerException.class,
                () -> Sample.class.getDeclaredField(null), "getDeclaredField(null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testMetadata();
        testPrimitiveRoundTrip();
        testStaticVsInstanceNull();
        testWrongObjectType();
        testAccessibility();
        testWrongPrimitiveAccessor();
        testFinalFieldSet();
        testEqualsHashCodeToString();
        testEnumConstantAndSynthetic();
        testAnnotations();
        testAccessFlags();
        testFieldDiscovery();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// Separate top-level class (not a nestmate of FieldTest) so reflection access to its
// private member requires setAccessible(true).
class ExtField {
    private String hidden;
}
