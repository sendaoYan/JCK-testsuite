/*
 * @test
 * @summary java.lang.reflect.AnnotatedType conformance: getType/getAnnotations on a
 *          return type, annotated and plain superclasses (null for Object, interfaces
 *          and annotation types), enum superclass as a parameterized type, annotated
 *          implemented interfaces in declaration order, interface-of-interface view,
 *          annotated receiver types on instance vs static executables, query surface
 *          on annotated types, and null argument behavior (recorded: getAnnotation
 *          and getDeclaredAnnotation tolerate null on this JDK release family).
 * @run main AnnotatedTypeTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public class AnnotatedTypeTest {
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

    // ---- fixtures ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE, ElementType.TYPE})
    @interface A {
    }

    static class Super {
    }

    static class SubAnno extends @A Super {
    }

    static class SubPlain extends Super {
    }

    static class SubGen extends ArrayList<@A String> {
    }

    interface Iface1 {
    }

    interface Iface2 {
    }

    interface SubIface extends Iface1 {
    }

    static class ImplTwo implements @A Iface1, Iface2 {
    }

    static class ImplNone {
    }

    @A
    @interface AnnoDecl {
    }

    enum Color {
        RED
    }

    static class Query {
        public @A String tagged;
        public String plain;
    }

    static class Receiver<T> {
        public void instance() {
        }

        public static void statik() {
        }

        public Receiver() {
        }
    }

    public static String returnsString() {
        return null;
    }

    static void testAnnotatedType() throws Exception {
        AnnotatedType at = AnnotatedTypeTest.class.getMethod("returnsString")
                .getAnnotatedReturnType();
        check(at != null, "getAnnotatedReturnType non-null");
        check(at.getType() == String.class, "getType == String");
        check(at.getAnnotations().length == 0, "getAnnotations");
        check(at.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
    }

    // superclass: annotated, plain, generic, and the null cases (Object, interface, annotation)
    static void testSuperclass() {
        AnnotatedType sc = SubAnno.class.getAnnotatedSuperclass();
        check(sc != null, "annotated superclass non-null");
        check(sc.getType() == Super.class, "annotated superclass type");
        check(sc.getAnnotation(A.class) != null, "superclass carries annotation");
        check(sc.isAnnotationPresent(A.class), "superclass annotation present");
        check(sc.getAnnotations().length == 1, "superclass one annotation");
        check(sc.getDeclaredAnnotations().length == 1, "superclass declared one");
        check(sc.getAnnotations()[0] == sc.getAnnotation(A.class), "same annotation instance");

        AnnotatedType plain = SubPlain.class.getAnnotatedSuperclass();
        check(plain != null, "plain superclass still non-null");
        check(plain.getType() == Super.class, "plain superclass type");
        check(plain.getAnnotations().length == 0, "plain superclass no annotations");

        check(Object.class.getAnnotatedSuperclass() == null, "Object itself has null superclass");
        check(Iface1.class.getAnnotatedSuperclass() == null, "interface superclass null");
        check(AnnoDecl.class.getAnnotatedSuperclass() == null, "annotation type superclass null");
        check(String.class.getAnnotatedSuperclass().getType() == Object.class,
              "implicit superclass is Object and non-null");

        AnnotatedType gsc = SubGen.class.getAnnotatedSuperclass();
        check(gsc instanceof AnnotatedParameterizedType, "generic superclass is parameterized");
        check(((AnnotatedParameterizedType) gsc).getAnnotatedActualTypeArguments()[0]
                .isAnnotationPresent(A.class), "generic superclass argument annotated");
        check(gsc.getType() instanceof ParameterizedType, "generic superclass underlying PT");
        check(((ParameterizedType) gsc.getType()).getRawType() == ArrayList.class,
              "generic superclass raw ArrayList");

        AnnotatedType esc = Color.class.getAnnotatedSuperclass();
        check(esc != null, "enum superclass non-null");
        check(esc.getType() instanceof ParameterizedType, "enum superclass is generic Enum");
        check(((ParameterizedType) esc.getType()).getRawType() == Enum.class, "enum superclass raw Enum");
        check(esc.getAnnotations().length == 0, "enum superclass unannotated");
    }

    // implemented interfaces: order, annotations, empty set, superinterfaces of an interface
    static void testInterfaces() {
        AnnotatedType[] ifs = ImplTwo.class.getAnnotatedInterfaces();
        check(ifs.length == 2, "two implemented interfaces");
        check(ifs[0].getType() == Iface1.class, "first interface in declaration order");
        check(ifs[1].getType() == Iface2.class, "second interface");
        check(ifs[0].getAnnotation(A.class) != null, "first interface annotated");
        check(ifs[1].getAnnotations().length == 0, "second interface unannotated");
        check(ifs[0].getDeclaredAnnotations().length == 1, "first interface declared one");

        check(ImplNone.class.getAnnotatedInterfaces().length == 0, "no interfaces empty array");
        check(Super.class.getAnnotatedInterfaces().length == 0, "plain class empty array");

        AnnotatedType[] supers = SubIface.class.getAnnotatedInterfaces();
        check(supers.length == 1, "interface extends one superinterface");
        check(supers[0].getType() == Iface1.class, "superinterface type");
        check(supers[0].getAnnotations().length == 0, "superinterface unannotated");

        // an annotation type implicitly implements java.lang.annotation.Annotation
        AnnotatedType[] annoIfs = AnnoDecl.class.getAnnotatedInterfaces();
        check(annoIfs.length == 1, "annotation type has one implicit interface");
        check(annoIfs[0].getType() == java.lang.annotation.Annotation.class,
              "implicit interface is Annotation");

        // Object implements none
        check(Object.class.getAnnotatedInterfaces().length == 0, "Object implements nothing");
    }

    // the annotated type of the class itself appears through the interface-of interest:
    // field use-site with TYPE_USE annotation and full query surface
    static void testQuerySurface() throws Exception {
        AnnotatedType tagged = Query.class.getField("tagged").getAnnotatedType();
        check(tagged.getType() == String.class, "tagged field underlying String");
        check(tagged.getAnnotation(A.class) != null, "tagged getAnnotation");
        check(tagged.isAnnotationPresent(A.class), "tagged present");
        check(tagged.getAnnotations().length == 1, "tagged one annotation");
        check(tagged.getAnnotationsByType(A.class).length == 1, "tagged byType one");
        check(tagged.getDeclaredAnnotation(A.class) != null, "tagged declared single");
        check(tagged.getDeclaredAnnotationsByType(A.class).length == 1, "tagged declared byType one");
        check(tagged.getAnnotations()[0].annotationType() == A.class, "annotation type is A");
        Annotation a = tagged.getAnnotation(A.class);
        check(a.equals(tagged.getDeclaredAnnotation(A.class)), "same value semantics");
        check(a.hashCode() == tagged.getDeclaredAnnotation(A.class).hashCode(), "hashCode stable");
        check(a.toString().contains("A"), "toString mentions A");
        check(tagged instanceof AnnotatedElement, "AnnotatedType is an AnnotatedElement");

        AnnotatedType plain = Query.class.getField("plain").getAnnotatedType();
        check(plain.getAnnotation(A.class) == null, "plain absent");
        check(!plain.isAnnotationPresent(A.class), "plain not present");
        check(plain.getAnnotationsByType(A.class).length == 0, "plain byType zero");
        check(plain.getDeclaredAnnotation(A.class) == null, "plain declared null");
    }

    // receiver type: instance method has one, static method and this-jdk constructor behavior
    static void testReceiverType() throws Exception {
        Method inst = Receiver.class.getMethod("instance");
        AnnotatedType rt = inst.getAnnotatedReceiverType();
        check(rt != null, "instance method receiver non-null");
        check(rt.getType() == Receiver.class, "receiver type is declaring class");
        check(rt.getAnnotations().length == 0, "receiver unannotated");

        Method stat = Receiver.class.getMethod("statik");
        check(stat.getAnnotatedReceiverType() == null, "static method receiver null");

        // On this JDK release the constructor of a top-level-style class reports a
        // non-null receiver (the enclosing class); recorded as observed behavior.
        Constructor<?> ctor = Receiver.class.getDeclaredConstructor();
        check(ctor.getAnnotatedReceiverType() != null,
              "constructor receiver non-null on this JDK");
    }

    // null argument behavior on AnnotatedType (recorded for this JDK family):
    // byType variants throw NPE; the single-value accessors return null/false silently.
    static void testNullArguments() throws Exception {
        final AnnotatedType at = Query.class.getField("tagged").getAnnotatedType();
        checkThrows(NullPointerException.class, () -> at.getAnnotationsByType(null),
              "getAnnotationsByType(null)");
        checkThrows(NullPointerException.class, () -> at.getDeclaredAnnotationsByType(null),
              "getDeclaredAnnotationsByType(null)");
        check(at.getAnnotation(null) == null, "getAnnotation(null) returns null (recorded)");
        check(!at.isAnnotationPresent(null), "isAnnotationPresent(null) false (recorded)");
        check(at.getDeclaredAnnotation(null) == null, "getDeclaredAnnotation(null) null (recorded)");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedType();
        testSuperclass();
        testInterfaces();
        testQuerySurface();
        testReceiverType();
        testNullArguments();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
