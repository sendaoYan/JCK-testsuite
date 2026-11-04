/*
 * @test
 * @summary java.lang.annotation.Annotation conformance: white-box structure + black-box
 *          annotationType/equals/hashCode/toString contract across marker, valued, and
 *          multi-member annotations (JDK 27 canonical toString formatting).
 * @run main AnnotationTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Annotation");
        check(c.isInterface(), "Annotation is an interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getMethods().length == 4, "exactly four abstract members");
        check(c.getMethod("equals", Object.class).getReturnType() == boolean.class, "equals");
        check(c.getMethod("hashCode").getReturnType() == int.class, "hashCode");
        check(c.getMethod("toString").getReturnType() == String.class, "toString");
        check(c.getMethod("annotationType").getReturnType() == Class.class, "annotationType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // annotationType / equals / hashCode on a marker annotation
        Annotation a = Annotated.class.getAnnotation(Marker.class);
        check(a != null, "marker present");
        check(a.annotationType() == Marker.class, "annotationType is @interface class");
        Annotation a2 = Annotated.class.getAnnotation(Marker.class);
        check(a.equals(a2), "equal instances per contract");
        check(a.hashCode() == a2.hashCode(), "hashCode consistent with equals");
        check(!a.equals(Annotated.class.getAnnotation(Valued.class)), "different types not equal");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("string"), "not equal to unrelated object");
        check(a.equals(a), "reflexive equals");

        // member-value semantics of the dynamic proxy: same values -> equal, different -> not
        Valued v = Annotated.class.getAnnotation(Valued.class);
        check(v.name().equals("x") && v.count() == 7, "member values readable");
        check(v.equals(a) == false, "Valued vs Marker not equal");
        check(v.hashCode() != 0, "member-based hashCode non-zero (name.hashCode + count)");

        // toString format: canonical name, members sorted by name, strings quoted
        String markerStr = a.toString();
        check(markerStr.equals("@AnnotationTest.Marker()"), "marker toString: " + markerStr);
        check(v.toString().equals("@AnnotationTest.Valued(count=7, name=\"x\")")
              || v.toString().equals("@AnnotationTest.Valued(name=\"x\", count=7)"),
              "valued toString: " + v.toString());

        // proxy nature: annotations are dynamic proxies, invocation handler maps members
        check(a instanceof java.lang.reflect.Proxy, "runtime instance is a Proxy");
        check(java.lang.reflect.Proxy.isProxyClass(a.getClass()), "proxy class per Proxy API");

        // hashCode contract: sum of (127 * memberName.hashCode()) ^ value.hashCode()
        Valued vOnlyName = new Valued() {
            @Override public String name() { return "x"; }
            @Override public int count() { return 7; }
            @Override public Class<? extends Annotation> annotationType() { return Valued.class; }
        };
        check(v.equals(vOnlyName), "proxy equals explicitly implemented annotation");
        int contract = (127 * "name".hashCode()) ^ "x".hashCode();
        contract += (127 * "count".hashCode()) ^ Integer.valueOf(7).hashCode();
        check(v.hashCode() == contract, "hashCode equals contract formula");

        // Annotation as AnnotatedElement family input
        Annotation[] all = Annotated.class.getAnnotations();
        check(all.length == 2, "two runtime-visible annotations");

        // ---- multi-member annotation: equals across types and members ----
        Rich rich = RichHolder.class.getAnnotation(Rich.class);
        Twin twin = TwinHolder.class.getAnnotation(Twin.class);
        check(rich != null && twin != null, "rich and twin both present");
        check(!rich.equals(twin), "different annotation types with same member values not equal");
        check(!twin.equals(rich), "cross-type inequality is symmetric");

        Rich richAgain = RichHolder.class.getAnnotation(Rich.class);
        check(rich.equals(richAgain), "same type same members equal");
        check(rich.hashCode() == richAgain.hashCode(), "hashCode consistent for equal members");
        Rich other = RichOtherHolder.class.getAnnotation(Rich.class);
        check(!rich.equals(other), "differing member values not equal");

        // every member kind readable with correct type
        check(rich.i() == 1, "int member");
        check(rich.b(), "boolean member");
        check("x".equals(rich.s()), "String member");
        check(rich.c() == String.class, "Class member");
        check(rich.p() == RetentionPolicy.RUNTIME, "enum member");
        check(Arrays.equals(rich.sa(), new String[] {"a", "b"}), "String[] member");
        check(Arrays.equals(rich.ia(), new int[] {1, 2}), "int[] member");
        check("t".equals(rich.n().tag()), "nested annotation member");
        check("defaultValue".equals(rich.dflt()), "defaulted member");

        // array members returned as defensive copies
        String[] saCopy = rich.sa();
        saCopy[0] = "mutated";
        check("a".equals(rich.sa()[0]), "String[] defensive copy");
        int[] iaCopy = rich.ia();
        iaCopy[0] = 12345;
        check(rich.ia()[0] == 1, "int[] defensive copy");
        check(rich.n().annotationType() == Nested.class, "nested annotationType");

        // rich toString: canonical name, quoted strings, Class via .class, arrays via {}
        String rs = rich.toString();
        check(rs.startsWith("@" + Rich.class.getCanonicalName() + "("), "rich prefix: " + rs);
        check(rs.endsWith(")"), "rich suffix");
        check(rs.contains("i=1"), "int member toString");
        check(rs.contains("b=true"), "boolean member toString");
        check(rs.contains("s=\"x\""), "String member quoted");
        check(rs.contains("c=java.lang.String.class"), "Class member .class form");
        check(rs.contains("p=RUNTIME"), "enum member name");
        check(rs.contains("sa={\"a\", \"b\"}"), "String[] member braces + quotes");
        check(rs.contains("ia={1, 2}"), "int[] member braces");
        check(rs.contains("dflt=\"defaultValue\""), "defaulted member quoted");
        check(rs.contains("n=@" + Nested.class.getCanonicalName() + "(tag=\"t\")"),
              "nested annotation toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
