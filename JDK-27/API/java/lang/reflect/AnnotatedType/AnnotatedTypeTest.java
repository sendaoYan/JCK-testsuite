/*
 * @test
 * @summary java.lang.reflect.AnnotatedType conformance: white-box structure + black-box behavior.
 * @run main AnnotatedTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class AnnotatedTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sink {
    }

    static class Types {
        public String plain;
        public @Sink String sunk;
        public List<String> list;
        public Map.Entry<String, Integer> entry;
    }

    public static String returnsString() {
        return null;
    }

    public static @Sink String returnsSunk() {
        return null;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        AnnotatedType at = AnnotatedTypeTest.class.getMethod("returnsString")
                .getAnnotatedReturnType();
        check(at != null, "getAnnotatedReturnType non-null");
        check(at.getType() == String.class, "getType == String");
        check(at.getAnnotations().length == 0, "getAnnotations");
        check(at.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
        check(AnnotatedType.class.isInterface(), "is an interface");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(AnnotatedType.class),
              "extends AnnotatedElement");
        check(!java.lang.reflect.Type.class.isAssignableFrom(AnnotatedType.class),
              "AnnotatedType wraps (not extends) Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. plain types carry no annotations but expose their underlying Type
        AnnotatedType plain = Types.class.getField("plain").getAnnotatedType();
        check(plain.getType() == String.class, "plain field underlying type");
        check(plain.getAnnotations().length == 0 && plain.getDeclaredAnnotations().length == 0,
              "plain no annotations");
        check(plain.getAnnotation(Sink.class) == null, "plain getAnnotation null");
        check(!plain.isAnnotationPresent(Sink.class), "plain not present");

        // 2. TYPE_USE annotations are visible through the contract
        AnnotatedType sunk = Types.class.getField("sunk").getAnnotatedType();
        check(sunk.getType() == String.class, "sunk underlying type");
        check(sunk.getAnnotation(Sink.class) != null, "sunk getAnnotation");
        check(sunk.isAnnotationPresent(Sink.class), "sunk isAnnotationPresent");
        check(sunk.getAnnotations().length == 1, "sunk one annotation");
        check(sunk.getDeclaredAnnotation(Sink.class) != null, "sunk declared annotation");

        // 3. method return types behave identically
        AnnotatedType rt = AnnotatedTypeTest.class.getMethod("returnsSunk").getAnnotatedReturnType();
        check(rt.getAnnotation(Sink.class) != null, "return type annotated");
        check(AnnotatedTypeTest.class.getMethod("returnsString").getAnnotatedReturnType()
              .getAnnotations().length == 0, "plain return type empty");
        check(rt.getType() == String.class, "return type underlying");

        // 4. owner type of an inner class annotated type
        AnnotatedType entryAt = Types.class.getField("entry").getAnnotatedType();
        AnnotatedType owner = entryAt.getAnnotatedOwnerType();
        check(owner != null && owner.getType() == Map.class, "Map.Entry owner is Map");
        check(owner.getAnnotations().length == 0, "owner unannotated");
        AnnotatedType plainAt = Types.class.getField("plain").getAnnotatedType();
        check(plainAt.getAnnotatedOwnerType() == null, "top-level type has no owner");

        // 5. parameterized underlying type stays fully generic
        AnnotatedType listAt = Types.class.getField("list").getAnnotatedType();
        check(listAt.getType() instanceof ParameterizedType, "List field underlying is PT");
        check(listAt.getType() == Types.class.getField("list").getGenericType()
              || listAt.getType().equals(Types.class.getField("list").getGenericType()),
              "underlying equals getGenericType");

        // 6. parameter annotated types
        Method m = AnnotatedTypeTest.class.getDeclaredMethod("consume", String.class);
        AnnotatedType pt = m.getParameters()[0].getAnnotatedType();
        check(pt.getType() == String.class, "parameter underlying type");
        check(pt.getAnnotation(Sink.class) != null, "parameter annotation visible");

        // 7. inherited default methods on the interface itself
        check(listAt.getAnnotationsByType(Sink.class).length == 0, "byType empty for unannotated");
        check(listAt.getDeclaredAnnotationsByType(Sink.class).length == 0,
              "declared-byType empty");
        boolean npe = false;
        try { listAt.getAnnotation(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getAnnotation(null) NPE");
    }

    static void consume(@Sink String s) {
    }

    // ---- Part 3: superclass / interfaces / receiver ----

    static class Super {
    }

    static class SubAnno extends @Sink Super {
    }

    static class SubPlain extends Super {
    }

    static class SubGen extends java.util.ArrayList<@Sink String> {
    }

    interface Iface1 {
    }

    interface Iface2 {
    }

    interface SubIface extends Iface1 {
    }

    static class ImplTwo implements @Sink Iface1, Iface2 {
    }

    static class ImplNone {
    }

    @interface AnnoDecl {
    }

    enum Color {
        RED
    }

    record Point(int x, int y) {
    }

    class InnerHost {
        InnerHost() {
        }
    }

    static class ReceiverHost<T> {
        public void instance() {
        }

        public static void statik() {
        }

        ReceiverHost() {
        }
    }

    // superclass view: annotated, plain, generic, null cases, enum and record shapes
    static void testSuperclass() {
        AnnotatedType sc = SubAnno.class.getAnnotatedSuperclass();
        check(sc != null, "annotated superclass non-null");
        check(sc.getType() == Super.class, "annotated superclass type");
        check(sc.getAnnotation(Sink.class) != null, "superclass carries annotation");
        check(sc.getAnnotations().length == 1, "superclass one annotation");

        AnnotatedType plain = SubPlain.class.getAnnotatedSuperclass();
        check(plain != null, "plain superclass non-null");
        check(plain.getType() == Super.class, "plain superclass type");
        check(plain.getAnnotations().length == 0, "plain superclass clean");

        check(Object.class.getAnnotatedSuperclass() == null, "Object itself null superclass");
        check(Iface1.class.getAnnotatedSuperclass() == null, "interface superclass null");
        check(AnnoDecl.class.getAnnotatedSuperclass() == null, "annotation type superclass null");
        check(String.class.getAnnotatedSuperclass().getType() == Object.class,
              "implicit superclass Object non-null");

        AnnotatedType gsc = SubGen.class.getAnnotatedSuperclass();
        check(gsc instanceof AnnotatedParameterizedType, "generic superclass parameterized");
        check(((AnnotatedParameterizedType) gsc).getAnnotatedActualTypeArguments()[0]
                .isAnnotationPresent(Sink.class), "generic superclass argument annotated");
        check(((ParameterizedType) gsc.getType()).getRawType() == java.util.ArrayList.class,
              "generic superclass raw ArrayList");

        AnnotatedType esc = Color.class.getAnnotatedSuperclass();
        check(esc != null, "enum superclass non-null");
        check(esc.getType() instanceof ParameterizedType, "enum superclass is generic Enum");
        check(((ParameterizedType) esc.getType()).getRawType() == Enum.class,
              "enum superclass raw Enum");

        AnnotatedType rsc = Point.class.getAnnotatedSuperclass();
        check(rsc != null, "record superclass non-null");
        check(rsc.getType() == java.lang.Record.class, "record superclass is Record");
    }

    // implemented interfaces: order, annotations, empties, superinterfaces
    static void testInterfaces() {
        AnnotatedType[] ifs = ImplTwo.class.getAnnotatedInterfaces();
        check(ifs.length == 2, "two interfaces in order");
        check(ifs[0].getType() == Iface1.class, "first interface");
        check(ifs[1].getType() == Iface2.class, "second interface");
        check(ifs[0].getAnnotation(Sink.class) != null, "first annotated");
        check(ifs[1].getAnnotations().length == 0, "second clean");

        check(ImplNone.class.getAnnotatedInterfaces().length == 0, "no interfaces empty");
        check(Object.class.getAnnotatedInterfaces().length == 0, "Object none");

        AnnotatedType[] supers = SubIface.class.getAnnotatedInterfaces();
        check(supers.length == 1, "interface has superinterface view");
        check(supers[0].getType() == Iface1.class, "superinterface type");

        AnnotatedType[] annoIfs = AnnoDecl.class.getAnnotatedInterfaces();
        check(annoIfs.length == 1, "annotation type one implicit interface");
        check(annoIfs[0].getType() == java.lang.annotation.Annotation.class,
              "implicit interface is Annotation");
    }

    // receiver types: generic declaring class is a parameterized annotated type on
    // modern JDKs; static methods and top-level-style constructors have none
    static void testReceiverType() throws Exception {
        AnnotatedType rt = ReceiverHost.class.getMethod("instance").getAnnotatedReceiverType();
        check(rt != null, "instance method receiver non-null");
        check(rt instanceof AnnotatedParameterizedType,
              "receiver of generic class is parameterized (recorded)");
        check(((ParameterizedType) rt.getType()).getRawType() == ReceiverHost.class,
              "receiver raw type is declaring class");
        check(rt.getAnnotations().length == 0, "receiver unannotated");

        check(ReceiverHost.class.getMethod("statik").getAnnotatedReceiverType() == null,
              "static method receiver null");
        check(ReceiverHost.class.getDeclaredConstructor().getAnnotatedReceiverType() == null,
              "top-level-style constructor receiver null (recorded)");

        AnnotatedType inner = InnerHost.class.getDeclaredConstructor(AnnotatedTypeTest.class)
                .getAnnotatedReceiverType();
        check(inner != null, "inner class constructor has receiver");
        check(inner.getType() == AnnotatedTypeTest.class, "inner receiver is enclosing instance");
    }

    // null arguments: every accessor throws NPE on modern JDKs
    static void testNullContract() throws Exception {
        final AnnotatedType at = Types.class.getField("plain").getAnnotatedType();
        boolean n1 = false;
        try { at.isAnnotationPresent(null); } catch (NullPointerException e) { n1 = true; }
        check(n1, "isAnnotationPresent(null) NPE");
        boolean n2 = false;
        try { at.getAnnotationsByType(null); } catch (NullPointerException e) { n2 = true; }
        check(n2, "getAnnotationsByType(null) NPE");
        boolean n3 = false;
        try { at.getDeclaredAnnotation(null); } catch (NullPointerException e) { n3 = true; }
        check(n3, "getDeclaredAnnotation(null) NPE");
        boolean n4 = false;
        try { at.getDeclaredAnnotationsByType(null); } catch (NullPointerException e) { n4 = true; }
        check(n4, "getDeclaredAnnotationsByType(null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSuperclass();
        testInterfaces();
        testReceiverType();
        testNullContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
