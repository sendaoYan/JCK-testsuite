/*
 * @test
 * @summary java.lang.annotation.Annotation conformance: annotationType/equals/hashCode/toString
 *          contract across marker, valued, multi-member, and cross-type comparisons (JDK 8
 *          legacy toString formatting).
 * @run main AnnotationTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class AnnotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Valued {
        String name();

        int count() default 1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Nested {
        String tag() default "n";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Rich {
        int i();

        boolean b();

        String s();

        Class<?> c();

        RetentionPolicy p();

        String[] sa();

        int[] ia();

        Nested n();

        String dflt() default "defaultValue";
    }

    // same member names/types as Rich, different annotation type name
    @Retention(RetentionPolicy.RUNTIME)
    @interface Twin {
        int i();

        boolean b();

        String s();

        Class<?> c();

        RetentionPolicy p();

        String[] sa();

        int[] ia();

        Nested n();

        String dflt() default "defaultValue";
    }

    @Marker
    @Valued(name = "x", count = 7)
    static class Annotated {
    }

    @Rich(i = 1, b = true, s = "x", c = String.class, p = RetentionPolicy.RUNTIME,
          sa = {"a", "b"}, ia = {1, 2}, n = @Nested(tag = "t"), dflt = "defaultValue")
    static class RichHolder {
    }

    @Twin(i = 1, b = true, s = "x", c = String.class, p = RetentionPolicy.RUNTIME,
          sa = {"a", "b"}, ia = {1, 2}, n = @Nested(tag = "t"), dflt = "defaultValue")
    static class TwinHolder {
    }

    @Rich(i = 99, b = false, s = "y", c = Integer.class, p = RetentionPolicy.CLASS,
          sa = {"z"}, ia = {9}, n = @Nested(tag = "u"))
    static class RichOtherHolder {
    }

    // every primitive member kind, no defaults (all must be specified at use site)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Prim {
        byte b();

        short s();

        char c();

        long l();

        float f();

        double d();

        boolean z();
    }

    @Prim(b = (byte) 1, s = (short) 2, c = 'x', l = 5L, f = 0.5f, d = 1.5, z = true)
    static class PrimHolder {
    }

    // every array member kind with default values (String[] plus eight primitive arrays)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArrAll {
        String[] sa() default {"a", "b"};

        int[] ia() default {1, 2};

        long[] la() default {3L};

        double[] da() default {1.5, 2.5};

        char[] ca() default {'x'};

        boolean[] ba() default {true, false};

        byte[] bta() default {(byte) 7};

        short[] sha() default {(short) 9};

        float[] fa() default {0.5f};
    }

    @ArrAll
    static class ArrHolder {
    }

    // Class-valued member: identity semantics of the returned Class and of hashCode
    @Retention(RetentionPolicy.RUNTIME)
    @interface WithClass {
        Class<?> c();
    }

    @WithClass(c = String.class)
    static class ClassHolder {
    }

    @WithClass(c = String.class)
    static class ClassHolderTwin {
    }

    @WithClass(c = Integer.class)
    static class ClassHolderOther {
    }

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Annotation");
        check(c.isInterface(), "Annotation is an interface");
        check(c.getMethods().length == 4, "exactly four abstract members");
        check(c.getMethod("equals", Object.class).getReturnType() == boolean.class, "equals returns boolean");
        check(c.getMethod("hashCode").getReturnType() == int.class, "hashCode returns int");
        check(c.getMethod("toString").getReturnType() == String.class, "toString returns String");
        check(c.getMethod("annotationType").getReturnType() == Class.class, "annotationType returns Class");
    }

    // ---- Part 2: annotationType / equals / hashCode ----
    static void testEqualsHashCode() {
        Marker a = Annotated.class.getAnnotation(Marker.class);
        check(a != null, "marker present");
        check(a.annotationType() == Marker.class, "annotationType is @interface class");

        Marker a2 = Annotated.class.getAnnotation(Marker.class);
        check(a.equals(a2), "equal marker instances");
        check(a.hashCode() == a2.hashCode(), "marker hashCode consistent");
        check(a.equals(a), "reflexive equals");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("string"), "not equal to unrelated object");
        check(!a.equals(Annotated.class.getAnnotation(Valued.class)), "different types not equal");

        // two different annotation types with identical member values are NOT equal
        Rich rich = RichHolder.class.getAnnotation(Rich.class);
        Twin twin = TwinHolder.class.getAnnotation(Twin.class);
        check(rich != null && twin != null, "rich and twin both present");
        check(!rich.equals(twin), "different annotation types with same member values not equal");
        check(!twin.equals(rich), "equality is not symmetric across types");

        // same type, same values -> equal; any member differs -> not equal
        Rich richAgain = RichHolder.class.getAnnotation(Rich.class);
        check(rich.equals(richAgain), "same type same values equal");
        check(rich.hashCode() == richAgain.hashCode(), "hashCode consistent for equal members");
        Rich other = RichOtherHolder.class.getAnnotation(Rich.class);
        check(!rich.equals(other), "differing member values not equal");

        // member values readable on the multi-member annotation
        check(rich.i() == 1, "int member");
        check(rich.b() == true, "boolean member");
        check("x".equals(rich.s()), "String member");
        check(rich.c() == String.class, "Class member");
        check(rich.p() == RetentionPolicy.RUNTIME, "enum member");
        check(Arrays.equals(rich.sa(), new String[] {"a", "b"}), "String[] member");
        check(Arrays.equals(rich.ia(), new int[] {1, 2}), "int[] member");
        check("t".equals(rich.n().tag()), "nested annotation member");
        check("defaultValue".equals(rich.dflt()), "defaulted member value");

        // array members are returned defensively copied (mutating the copy does not
        // affect the underlying annotation data)
        String[] saCopy = rich.sa();
        saCopy[0] = "mutated";
        check("a".equals(rich.sa()[0]), "String[] defensive copy");
        int[] iaCopy = rich.ia();
        iaCopy[0] = 12345;
        check(rich.ia()[0] == 1, "int[] defensive copy");

        // nested annotation member round-trips annotationType
        check(rich.n().annotationType() == Nested.class, "nested annotationType");

        // hashCode formula on the simple valued annotation: sum of
        // (127 * memberName.hashCode()) ^ memberValue.hashCode()
        Valued v = Annotated.class.getAnnotation(Valued.class);
        int contract = (127 * "name".hashCode()) ^ "x".hashCode();
        contract += (127 * "count".hashCode()) ^ Integer.valueOf(7).hashCode();
        check(v.hashCode() == contract, "hashCode equals spec formula");
    }

    // ---- Part 3: toString (JDK 8 legacy format) ----
    static void testToString() {
        Marker a = Annotated.class.getAnnotation(Marker.class);
        check(a.toString().equals("@" + Marker.class.getName() + "()"),
              "marker toString: " + a.toString());

        // JDK 8 toString: binary name, unquoted strings, Class via Class.toString(),
        // arrays with [], nested annotation with its own legacy form
        Rich rich = RichHolder.class.getAnnotation(Rich.class);
        String rs = rich.toString();
        check(rs.startsWith("@" + Rich.class.getName() + "("), "rich prefix: " + rs);
        check(rs.endsWith(")"), "rich suffix");
        check(rs.contains("i=1"), "int member toString");
        check(rs.contains("b=true"), "boolean member toString");
        check(rs.contains("s=x"), "String member toString (unquoted)");
        check(rs.contains("c=class java.lang.String"), "Class member toString (legacy)");
        check(rs.contains("p=RUNTIME"), "enum member toString");
        check(rs.contains("sa=[a, b]"), "String[] member toString (legacy)");
        check(rs.contains("ia=[1, 2]"), "int[] member toString (legacy)");
        check(rs.contains("dflt=defaultValue"), "defaulted member toString (unquoted)");
        check(rs.contains("n=@" + Nested.class.getName() + "(tag=t)"), "nested annotation toString");

        Valued v = Annotated.class.getAnnotation(Valued.class);
        check(v.toString().equals("@" + Valued.class.getName() + "(name=x, count=7)")
              || v.toString().equals("@" + Valued.class.getName() + "(count=7, name=x)"),
              "valued toString (member order unspecified): " + v.toString());
    }

    // ---- Part 4: proxy nature + AnnotatedElement integration ----
    static void testProxy() {
        Marker a = Annotated.class.getAnnotation(Marker.class);
        check(a instanceof Proxy, "runtime instance is a Proxy");
        check(Proxy.isProxyClass(a.getClass()), "proxy class per Proxy API");

        Annotation[] all = Annotated.class.getAnnotations();
        boolean sawMarker = false, sawValued = false;
        for (Annotation ann : all) {
            if (ann.annotationType() == Marker.class) sawMarker = true;
            if (ann.annotationType() == Valued.class) sawValued = true;
        }
        check(sawMarker && sawValued, "getAnnotations lists both runtime-visible annotations");
        check(Annotated.class.getAnnotation(Marker.class).annotationType() == Marker.class,
              "getAnnotation lookup by type");
    }

    // ---- Part 5: interface white box ----
    static void testInterfaceWhiteBox() throws Exception {
        // Annotation declares exactly the four contract methods, all abstract, and no
        // default methods of any kind (verified on both JDK 8 and JDK 27 via javap)
        java.lang.reflect.Method[] ms = Annotation.class.getDeclaredMethods();
        check(ms.length == 4, "exactly four declared methods: " + ms.length);
        int abstractCount = 0;
        for (java.lang.reflect.Method m : ms) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 4, "all four methods abstract");
        check(Annotation.class.getMethod("equals", Object.class).getParameterTypes().length == 1,
              "equals takes exactly one parameter");
        check(Annotation.class.getMethod("hashCode").getParameterTypes().length == 0,
              "hashCode takes no parameters");
        check(Annotation.class.getMethod("annotationType").getParameterTypes().length == 0,
              "annotationType takes no parameters");
        check(Annotation.class.getDeclaredFields().length == 0, "no declared fields on the interface");
    }

    // ---- Part 6: member proxy full family (primitives, arrays, Class) ----
    static void testDeepenedMemberContract() {
        Prim p = PrimHolder.class.getAnnotation(Prim.class);
        check(p != null, "Prim present");
        check(p.b() == (byte) 1, "byte member");
        check(p.s() == (short) 2, "short member");
        check(p.c() == 'x', "char member");
        check(p.l() == 5L, "long member");
        check(p.f() == 0.5f, "float member");
        check(p.d() == 1.5, "double member");
        check(p.z() == true, "boolean member");
        check(p.annotationType() == Prim.class, "Prim annotationType");
        check(p.equals(PrimHolder.class.getAnnotation(Prim.class)), "Prim equal across two fetches");
        int fp = 0;
        fp += (127 * "b".hashCode()) ^ Byte.valueOf((byte) 1).hashCode();
        fp += (127 * "s".hashCode()) ^ Short.valueOf((short) 2).hashCode();
        fp += (127 * "c".hashCode()) ^ Character.valueOf('x').hashCode();
        fp += (127 * "l".hashCode()) ^ Long.valueOf(5L).hashCode();
        fp += (127 * "f".hashCode()) ^ Float.valueOf(0.5f).hashCode();
        fp += (127 * "d".hashCode()) ^ Double.valueOf(1.5).hashCode();
        fp += (127 * "z".hashCode()) ^ Boolean.valueOf(true).hashCode();
        check(p.hashCode() == fp, "Prim hashCode equals spec formula over all primitives");
        check(p.hashCode() == PrimHolder.class.getAnnotation(Prim.class).hashCode(),
              "Prim hashCode stable across fetches");
        String ps = p.toString();
        check(ps.startsWith("@" + Prim.class.getName() + "("), "Prim toString prefix: " + ps);
        check(ps.endsWith(")"), "Prim toString suffix");

        ArrAll ar = ArrHolder.class.getAnnotation(ArrAll.class);
        check(ar != null, "ArrAll present with all-default members");
        check(Arrays.equals(ar.sa(), new String[] {"a", "b"}), "String[] default value");
        check(ar.la()[0] == 3L, "long[] default value");
        check(ar.da()[0] == 1.5 && ar.da()[1] == 2.5, "double[] default value");
        check(ar.ca()[0] == 'x', "char[] default value");
        check(ar.ba()[0] == true && ar.ba()[1] == false, "boolean[] default value");
        check(ar.bta()[0] == (byte) 7, "byte[] default value");
        check(ar.sha()[0] == (short) 9, "short[] default value");
        check(ar.fa()[0] == 0.5f, "float[] default value");
        check(Arrays.equals(ar.ia(), new int[] {1, 2}), "int[] default value");
        // defensive copies on every array member kind returned by the proxy
        long[] laCopy = ar.la();
        laCopy[0] = 999L;
        check(ar.la()[0] == 3L, "long[] defensive copy");
        double[] daCopy = ar.da();
        daCopy[0] = 999.0;
        check(ar.da()[0] == 1.5, "double[] defensive copy");
        boolean[] baCopy = ar.ba();
        baCopy[0] = false;
        check(ar.ba()[0] == true, "boolean[] defensive copy");
        char[] caCopy = ar.ca();
        caCopy[0] = 'q';
        check(ar.ca()[0] == 'x', "char[] defensive copy");
        // array hashCode formula: Arrays.hashCode per member, XOR 127 * name hash
        int fa = 0;
        fa += (127 * "sa".hashCode()) ^ Arrays.hashCode(new String[] {"a", "b"});
        fa += (127 * "ia".hashCode()) ^ Arrays.hashCode(new int[] {1, 2});
        fa += (127 * "la".hashCode()) ^ Arrays.hashCode(new long[] {3L});
        fa += (127 * "da".hashCode()) ^ Arrays.hashCode(new double[] {1.5, 2.5});
        fa += (127 * "ca".hashCode()) ^ Arrays.hashCode(new char[] {'x'});
        fa += (127 * "ba".hashCode()) ^ Arrays.hashCode(new boolean[] {true, false});
        fa += (127 * "bta".hashCode()) ^ Arrays.hashCode(new byte[] {(byte) 7});
        fa += (127 * "sha".hashCode()) ^ Arrays.hashCode(new short[] {(short) 9});
        fa += (127 * "fa".hashCode()) ^ Arrays.hashCode(new float[] {0.5f});
        check(ar.hashCode() == fa, "ArrAll hashCode uses Arrays.hashCode per member");
        check(ar.annotationType() == ArrAll.class, "ArrAll annotationType");

        // Class-valued member: identity == class literal, and annotation equality is
        // purely value-based -- two DIFFERENT elements carrying the same values are equal
        WithClass w1 = ClassHolder.class.getAnnotation(WithClass.class);
        WithClass w2 = ClassHolderTwin.class.getAnnotation(WithClass.class);
        WithClass w3 = ClassHolderOther.class.getAnnotation(WithClass.class);
        check(w1 != null && w2 != null && w3 != null, "WithClass instances present");
        check(w1.c() == String.class, "Class member returns the class literal identity");
        check(w1.equals(w2), "same member values on different elements are equal");
        check(w1.hashCode() == w2.hashCode(), "hashCode equal for value-equal instances");
        check(!w1.equals(w3), "different Class member values not equal");
        // the hash of a Class member is the Class object's own hashCode (identity hash,
        // deterministic within one JVM run because both sides use the same Class object)
        check(w1.hashCode() == ((127 * "c".hashCode()) ^ String.class.hashCode()),
              "Class member hashCode is (127 * name) ^ class.hashCode()");

        // a marker annotation has no members: spec formula degenerates to zero
        Marker m = Annotated.class.getAnnotation(Marker.class);
        check(m.hashCode() == 0, "marker annotation hashCode is 0");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testEqualsHashCode();
        testToString();
        testProxy();
        testInterfaceWhiteBox();
        testDeepenedMemberContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
