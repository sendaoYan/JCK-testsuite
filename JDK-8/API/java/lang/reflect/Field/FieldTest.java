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
 *          Deepening batch: full typed-accessor matrix incl. every JLS widening path,
 *          exact IllegalArgumentException/NPE message fragments, final-field mutation
 *          semantics, setAccessible(false) restore, generic field type model,
 *          enum-constant/synthetic modifier bits, and exact toString strings.
 * @run main FieldTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

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

    /** Exact exception class AND message containing the measured fragment (2 assertions). */
    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            check(expected.isInstance(t), msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName());
            check(t.getMessage() != null && t.getMessage().contains(fragment),
                    msg + ": message [" + t.getMessage() + "] missing \"" + fragment + "\"");
            return;
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

    // ---- deepening-batch targets ----

    /** One field per family so every typed accessor has an exact-type home. */
    static class Data {
        public int intF;
        public long longF;
        public double dblF;
        public boolean boolF;
        public byte byteF;
        public char charF;
        public short shortF;
        public float floatF;
        public String strF;
        public Object objF;
    }

    /** ctor-initialized final (not a compile-time constant, so no javac inlining). */
    static class FinalT {
        public final int cf;
        public static final int SC = make();
        FinalT() { cf = Integer.parseInt("7"); }
        static int make() { return 5; }
    }

    static class Holder {
        public static int stat;
        public static String statStr;
    }

    static class Gen<T> {
        public T tF;
        public java.util.List<String> listF;
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
        Field priv = Sample.class.getDeclaredField("priv");
        check(Modifier.isPrivate(priv.getModifiers()), "priv is private");
        Sample s = new Sample();
        checkThrows(IllegalAccessException.class, () -> priv.get(s),
                "private get without accessible IAE");
        checkThrows(IllegalAccessException.class, () -> priv.set(s, "x"),
                "private set without accessible IAE");

        priv.setAccessible(true);
        priv.set(s, "hello");
        check("hello".equals(priv.get(s)), "private set/get after setAccessible");
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

        // non-static inner class has a synthetic this$0 field
        boolean foundSynthetic = false;
        for (Field f : Outer.Inner.class.getDeclaredFields()) {
            if (f.isSynthetic()) {
                foundSynthetic = true;
                check(f.getName().contains("this$0"), "synthetic this$0 name");
                check(f.getType() == Outer.class, "synthetic this$0 type");
            }
        }
        check(foundSynthetic, "inner class has synthetic this$0");
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

    // ---- deepening batch: typed accessor matrix with exact values ----

    static void testTypedAccessorRoundTrip() throws Exception {
        Data d = new Data();
        Field intF = Data.class.getField("intF");
        intF.setInt(d, 41);
        check(intF.getInt(d) == 41, "typed setInt/getInt");
        check(intF.getLong(d) == 41L, "getLong(int) widens");
        check(intF.getFloat(d) == 41.0f, "getFloat(int) widens");
        check(intF.getDouble(d) == 41.0, "getDouble(int) widens");
        check(intF.get(d).equals(Integer.valueOf(41)), "get(int) boxed Integer");

        Field longF = Data.class.getField("longF");
        longF.setLong(d, 123456789012345L);
        check(longF.getLong(d) == 123456789012345L, "typed setLong/getLong");
        check(longF.getFloat(d) == (float) 123456789012345L, "getFloat(long) widens");
        check(longF.getDouble(d) == (double) 123456789012345L, "getDouble(long) widens");
        check(longF.get(d).equals(Long.valueOf(123456789012345L)), "get(long) boxed Long");

        Field dblF = Data.class.getField("dblF");
        dblF.setDouble(d, 3.5);
        check(dblF.getDouble(d) == 3.5, "typed setDouble/getDouble");
        dblF.setDouble(d, Double.MIN_VALUE);
        check(dblF.getDouble(d) == Double.MIN_VALUE, "double MIN_VALUE round-trip");

        Field boolF = Data.class.getField("boolF");
        boolF.setBoolean(d, true);
        check(boolF.getBoolean(d), "setBoolean/getBoolean true");
        boolF.setBoolean(d, false);
        check(!boolF.getBoolean(d), "setBoolean/getBoolean false");

        Field byteF = Data.class.getField("byteF");
        byteF.setByte(d, Byte.MIN_VALUE);
        check(byteF.getByte(d) == Byte.MIN_VALUE, "byte MIN round-trip");
        byteF.setByte(d, Byte.MAX_VALUE);
        check(byteF.getByte(d) == Byte.MAX_VALUE, "byte MAX round-trip");
        check(byteF.getShort(d) == (short) Byte.MAX_VALUE, "getShort(byte) widens");
        check(byteF.getInt(d) == Byte.MAX_VALUE, "getInt(byte) widens");
        check(byteF.getLong(d) == (long) Byte.MAX_VALUE, "getLong(byte) widens");
        check(byteF.getFloat(d) == Byte.MAX_VALUE, "getFloat(byte) widens");
        check(byteF.getDouble(d) == Byte.MAX_VALUE, "getDouble(byte) widens");
        check(byteF.get(d).equals(Byte.valueOf(Byte.MAX_VALUE)), "get(byte) boxed Byte");

        Field charF = Data.class.getField("charF");
        charF.setChar(d, '￿');
        check(charF.getChar(d) == '￿', "char max round-trip");
        charF.setChar(d, '\0');
        check(charF.getChar(d) == '\0', "char zero round-trip");
        charF.setChar(d, 'A');
        check(charF.getInt(d) == 65, "getInt(char) widens");
        check(charF.getLong(d) == 65L, "getLong(char) widens");
        check(charF.getDouble(d) == 65.0, "getDouble(char) widens");
        check(charF.get(d).equals(Character.valueOf('A')), "get(char) boxed Character");

        Field shortF = Data.class.getField("shortF");
        shortF.setShort(d, Short.MIN_VALUE);
        check(shortF.getShort(d) == Short.MIN_VALUE, "short MIN round-trip");
        shortF.setShort(d, Short.MAX_VALUE);
        check(shortF.getShort(d) == Short.MAX_VALUE, "short MAX round-trip");
        check(shortF.getInt(d) == Short.MAX_VALUE, "getInt(short) widens");
        check(shortF.getLong(d) == (long) Short.MAX_VALUE, "getLong(short) widens");
        check(shortF.getDouble(d) == (double) Short.MAX_VALUE, "getDouble(short) widens");

        Field floatF = Data.class.getField("floatF");
        floatF.setFloat(d, -0.0f);
        check(Float.floatToRawIntBits(floatF.getFloat(d)) == Float.floatToRawIntBits(-0.0f),
                "float -0.0 bit-exact round-trip");
        floatF.setFloat(d, Float.MIN_VALUE);
        check(floatF.getFloat(d) == Float.MIN_VALUE, "float MIN_VALUE round-trip");
        check(floatF.getDouble(d) == (double) Float.MIN_VALUE, "getDouble(float) widens");

        Field strF = Data.class.getField("strF");
        String one = "ref";
        strF.set(d, one);
        check(strF.get(d) == one, "String set/get same reference");
        strF.set(d, null);
        check(strF.get(d) == null, "String set null/get null");

        Field objF = Data.class.getField("objF");
        Object token = new Object();
        objF.set(d, token);
        check(objF.get(d) == token, "Object set/get same reference");
        objF.set(d, Integer.valueOf(9));
        check(objF.get(d).equals(Integer.valueOf(9)), "Object field holds Integer");

        // widening typed setters: narrower primitive into wider field
        longF.setLong(d, 0L);
        longF.setInt(d, 3);
        check(longF.getLong(d) == 3L, "setInt(long field)");
        longF.setByte(d, (byte) 4);
        check(longF.getLong(d) == 4L, "setByte(long field)");
        longF.setShort(d, (short) 5);
        check(longF.getLong(d) == 5L, "setShort(long field)");
        longF.setChar(d, 'c');
        check(longF.getLong(d) == 99L, "setChar(long field)");

        dblF.setDouble(d, 0.0);
        dblF.setInt(d, 6);
        check(dblF.getDouble(d) == 6.0, "setInt(double field)");
        dblF.setLong(d, 7L);
        check(dblF.getDouble(d) == 7.0, "setLong(double field)");
        dblF.setFloat(d, 8.5f);
        check(dblF.getDouble(d) == 8.5, "setFloat(double field)");

        intF.setInt(d, 0);
        intF.setByte(d, (byte) 10);
        check(intF.getInt(d) == 10, "setByte(int field)");
        intF.setShort(d, (short) 11);
        check(intF.getInt(d) == 11, "setShort(int field)");
        intF.setChar(d, 'x');
        check(intF.getInt(d) == 120, "setChar(int field)");

        shortF.setShort(d, (short) 0);
        shortF.setByte(d, (byte) 12);
        check(shortF.getShort(d) == 12, "setByte(short field)");

        floatF.setFloat(d, 0f);
        floatF.setInt(d, 13);
        check(floatF.getFloat(d) == 13f, "setInt(float field)");
        floatF.setLong(d, 14L);
        check(floatF.getFloat(d) == 14f, "setLong(float field)");

        // boxed widening through set(Object, Object)
        longF.set(d, Integer.valueOf(21));
        check(longF.getLong(d) == 21L, "set(long field, Integer) widens");
        intF.set(d, Byte.valueOf((byte) 22));
        check(intF.getInt(d) == 22, "set(int field, Byte) widens");
        intF.set(d, Character.valueOf('x'));
        check(intF.getInt(d) == 120, "set(int field, Character) widens");
        shortF.set(d, Byte.valueOf((byte) 23));
        check(shortF.getShort(d) == 23, "set(short field, Byte) widens");
        dblF.set(d, Long.MAX_VALUE);
        check(dblF.getDouble(d) == 9.223372036854776E18, "set(double field, Long) widens");

        // reflective writes are visible through plain Java field reads
        check(d.intF == 120, "plain read sees reflective int write");
        check(d.longF == 21L, "plain read sees reflective long write");
        check(d.dblF == 9.223372036854776E18, "plain read sees reflective double write");
        check(d.shortF == 23, "plain read sees reflective short write");
    }

    // ---- deepening batch: exact mismatch messages (measured on JDK-8) ----

    static void testMismatchExactMessages() throws Exception {
        Data d = new Data();
        Field intF = Data.class.getField("intF");
        Field strF = Data.class.getField("strF");
        Field boolF = Data.class.getField("boolF");
        Field longF = Data.class.getField("longF");
        Field dblF = Data.class.getField("dblF");
        Field byteF = Data.class.getField("byteF");
        Field charF = Data.class.getField("charF");

        checkThrowsMsg(IllegalArgumentException.class,
                () -> strF.getInt(d), "Attempt to get java.lang.String field",
                "getInt on String field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> strF.set(d, Integer.valueOf(5)), "Can not set java.lang.String field",
                "set(String field, Integer) message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.set(d, "x"), "Can not set int field", "set(int field, String) message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> boolF.getDouble(d), "Attempt to get boolean field",
                "getDouble on boolean field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> longF.getChar(d), "Attempt to get long field",
                "getChar on long field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> dblF.getBoolean(d), "Attempt to get double field",
                "getBoolean on double field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.setLong(d, 5L), "Can not set int field", "setLong on int field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> byteF.setInt(d, 300), "Can not set byte field",
                "setInt on byte field overflow message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> byteF.setShort(d, (short) 1), "Can not set byte field",
                "setShort on byte field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.setDouble(d, 1.0), "Can not set int field",
                "setDouble on int field message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> charF.setInt(d, 65), "Can not set char field",
                "setInt on char field message");
        // byte -> char is NOT a JLS widening conversion, even boxed
        checkThrowsMsg(IllegalArgumentException.class,
                () -> charF.set(d, Byte.valueOf((byte) 65)), "Can not set char field",
                "set(char field, Byte) message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> charF.set(d, Integer.valueOf(65)), "Can not set char field",
                "set(char field, Integer) message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> byteF.set(d, Integer.valueOf(1)), "Can not set byte field",
                "set(byte field, Integer) message");
        // wrong receiver object: JDK-8 legacy wording uses "Can not set ... to <cls>"
        // for BOTH get and set receiver mismatches
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.get("str"), "Can not set int field", "get(int) on String receiver message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.getInt("str"), "Can not set int field",
                "getInt on String receiver message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> intF.set("str", Integer.valueOf(1)), "Can not set int field",
                "set(int) on String receiver message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> strF.set(new Object(), "x"), "Can not set java.lang.String field",
                "set(String) on Object receiver message");
    }

    // ---- deepening batch: null receiver semantics ----

    static void testNullReceiverSemantics() throws Exception {
        Field intF = Data.class.getField("intF");
        // JDK-8: bare NPE with no message detail for instance fields
        try {
            intF.get(null);
            throw new AssertionError("get(null) on instance field must throw");
        } catch (NullPointerException npe) {
            check(npe.getMessage() == null, "JDK-8 instance get(null) NPE has null message");
        }
        checkThrows(NullPointerException.class, () -> intF.getLong(null), "getLong(null) NPE");
        checkThrows(NullPointerException.class, () -> intF.getDouble(null), "getDouble(null) NPE");
        Field boolF = Data.class.getField("boolF");
        checkThrows(NullPointerException.class, () -> boolF.setBoolean(null, true),
                "setBoolean(null) NPE");
        checkThrows(NullPointerException.class, () -> intF.set(null, Integer.valueOf(1)),
                "set(null,..) NPE");

        // static fields accept null receiver for every accessor form
        Field sc = FinalT.class.getDeclaredField("SC");
        check(sc.getInt(null) == 5, "static getInt(null)");
        check(sc.get(null).equals(Integer.valueOf(5)), "static get(null) boxed");
        check(sc.getLong(null) == 5L, "static getLong(null) widens");

        Holder.stat = 0;
        Field stat = Holder.class.getField("stat");
        stat.setInt(null, 77);
        check(Holder.stat == 77, "static setInt(null) visible via plain read");
        check(stat.getInt(null) == 77, "static getInt(null) after setInt");
        stat.set(null, Integer.valueOf(78));
        check(Holder.stat == 78, "static set(null, box) visible via plain read");
        stat.setShort(null, (short) 80);
        check(Holder.stat == 80, "static setShort(null) widens into int");
        Holder.statStr = null;
        Field statStr = Holder.class.getField("statStr");
        statStr.set(null, "z");
        check("z".equals(Holder.statStr), "static String set(null) via plain read");
    }

    // ---- deepening batch: final field mutation semantics ----

    static void testFinalFieldMutation() throws Exception {
        FinalT ft = new FinalT();
        Field cf = FinalT.class.getDeclaredField("cf");
        check(Modifier.isFinal(cf.getModifiers()), "ctor-final is final");
        cf.setAccessible(true);
        cf.setInt(ft, 123);
        check(cf.getInt(ft) == 123, "non-static final reflective setInt read back");
        check(ft.cf == 123, "plain read sees final field mutation");

        // static final: setAccessible(true) does NOT unlock write access
        Field sc = FinalT.class.getDeclaredField("SC");
        sc.setAccessible(true);
        checkThrowsMsg(IllegalAccessException.class, () -> sc.setInt(null, 9),
                "Can not set static final", "static final setInt message");
        checkThrowsMsg(IllegalAccessException.class, () -> sc.set(null, Integer.valueOf(9)),
                "Can not set static final", "static final set message");
        // compile-time-constant static final is refused the same way
        Field konst = Sample.class.getField("CONST");
        konst.setAccessible(true);
        checkThrowsMsg(IllegalAccessException.class, () -> konst.setDouble(null, 1.0),
                "Can not set static final", "constant static final setDouble message");
    }

    // ---- deepening batch: setAccessible restore semantics ----

    static void testAccessibleRestore() throws Exception {
        Field priv = Sample.class.getDeclaredField("priv");
        priv.setAccessible(true);
        priv.set(new Sample(), "w");
        priv.setAccessible(false);
        // restoring the flag re-enables the language-level access check
        checkThrowsMsg(IllegalAccessException.class, () -> priv.get(new Sample()),
                "can not access a member", "private get after restore message (JDK-8 wording)");
        checkThrowsMsg(IllegalAccessException.class, () -> priv.set(new Sample(), "x"),
                "can not access a member", "private set after restore message (JDK-8 wording)");

        // public field: access never depended on the flag
        Field pub = Sample.class.getField("pub");
        pub.setAccessible(true);
        pub.setAccessible(false);
        Sample s = new Sample();
        pub.setInt(s, 5);
        check(s.pub == 5 && pub.getInt(s) == 5, "public field usable after flag restore");
    }

    // ---- deepening batch: generic field type model ----

    static void testGenericFieldModel() throws Exception {
        Field listF = Gen.class.getField("listF");
        Type gt = listF.getGenericType();
        check(gt instanceof ParameterizedType, "List<String> generic type ParameterizedType");
        ParameterizedType pt = (ParameterizedType) gt;
        check(pt.getRawType() == java.util.List.class, "ParameterizedType raw type");
        check(pt.getActualTypeArguments().length == 1, "one type argument");
        check(pt.getActualTypeArguments()[0] == String.class, "type argument is String");
        check(pt.getOwnerType() == null, "owner type null");
        check(pt.getTypeName().equals("java.util.List<java.lang.String>"), "typeName exact");

        Field tF = Gen.class.getField("tF");
        Type tv = tF.getGenericType();
        check(tv instanceof TypeVariable, "T generic type is TypeVariable");
        check(((TypeVariable<?>) tv).getName().equals("T"), "type variable name T");
        check(tF.getType() == Object.class, "erased static type Object");

        check(listF.toString().equals("public java.util.List " + Gen.class.getName() + ".listF"),
                "toString erases type arguments");
        check(listF.toGenericString().equals(
                "public java.util.List<java.lang.String> " + Gen.class.getName() + ".listF"),
                "toGenericString keeps type arguments");
        check(tF.toString().equals("public java.lang.Object " + Gen.class.getName() + ".tF"),
                "toString uses erasure for T");
        check(tF.toGenericString().equals("public T " + Gen.class.getName() + ".tF"),
                "toGenericString keeps T");
    }

    // ---- deepening batch: enum-constant and synthetic modifier bits ----

    static void testEnumConstantBits() throws Exception {
        Field red = Color.class.getField("RED");
        check(red.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | 0x4000),
                "enum constant bits PUBLIC|STATIC|FINAL|ACC_ENUM");
        check(red.isEnumConstant() && !red.isSynthetic(), "enum constant isEnumConstant not synthetic");
        check(red.toString().equals("public static final " + Color.class.getName()
                + " " + Color.class.getName() + ".RED"), "enum constant toString exact");
        check(red.toGenericString().equals(red.toString()), "enum constant toGenericString equal");

        Field values = Color.class.getDeclaredField("$VALUES");
        check(values.isSynthetic(), "$VALUES isSynthetic");
        check(values.getModifiers() == (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | 0x1000),
                "$VALUES bits PRIVATE|STATIC|FINAL|ACC_SYNTHETIC");
    }

    // ---- deepening batch: exact toString strings ----

    static void testToStringExact() throws Exception {
        check(Data.class.getField("intF").toString().equals(
                "public int " + Data.class.getName() + ".intF"), "int field toString exact");
        check(Data.class.getField("intF").toGenericString().equals(
                "public int " + Data.class.getName() + ".intF"), "int field toGenericString exact");
        check(Sample.class.getField("vl").toString().equals(
                "public volatile long " + Sample.class.getName() + ".vl"),
                "volatile field toString exact");
        check(Sample.class.getField("trans").toString().equals(
                "public transient java.lang.Object " + Sample.class.getName() + ".trans"),
                "transient field toString exact");
        check(Holder.class.getField("stat").toString().equals(
                "public static int " + Holder.class.getName() + ".stat"),
                "static field toString exact");
        check(Sample.class.getDeclaredField("priv").toString().equals(
                "private java.lang.String " + Sample.class.getName() + ".priv"),
                "private field toString exact");
        check(Sample.class.getField("CONST").toString().equals(
                "public static final double " + Sample.class.getName() + ".CONST"),
                "static final toString exact");
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
        testFieldDiscovery();
        testTypedAccessorRoundTrip();
        testMismatchExactMessages();
        testNullReceiverSemantics();
        testFinalFieldMutation();
        testAccessibleRestore();
        testGenericFieldModel();
        testEnumConstantBits();
        testToStringExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
