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

    public static void main(String[] args) throws Exception {
        testStructural();
        testEqualsHashCode();
        testToString();
        testProxy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
