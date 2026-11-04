/*
 * @test
 * @summary java.lang.annotation.Annotation conformance: white-box structure + black-box behavior.
 * @run main AnnotationTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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

    @Marker
    @Valued(name = "x", count = 7)
    static class Annotated {
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

        // member-value semantics of the dynamic proxy: same values -> equal, different -> not
        Valued v = Annotated.class.getAnnotation(Valued.class);
        check(v.name().equals("x") && v.count() == 7, "member values readable");
        check(v.equals(a) == false, "Valued vs Marker not equal");
        check(v.hashCode() != 0, "member-based hashCode non-zero (name.hashCode + count)");

        // toString format: canonical name, members sorted by name, strings quoted
        String markerStr = a.toString();
        check(markerStr.equals("@AnnotationTest.Marker()"), "marker toString: " + markerStr);
        check(v.toString().equals("@AnnotationTest.Valued(count=7, name=\"x\")"),
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
