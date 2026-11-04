/*
 * @test
 * @summary java.text.Annotation conformance: white-box structure + black-box behavior.
 * @run main AnnotationTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class AnnotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.Annotation");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, non-final");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPublic(ctors[0].getModifiers()),
                "single public Annotation(Object)");
        check(ctors[0].getParameterTypes()[0] == Object.class, "ctor takes Object value");

        Method getValue = c.getMethod("getValue");
        check(getValue.getReturnType() == Object.class && Modifier.isPublic(getValue.getModifiers()),
                "public Object getValue()");
        Method toString = c.getMethod("toString");
        check(toString.getReturnType() == String.class, "public String toString()");
        check(c.getDeclaredMethods().length == 2, "exactly getValue/toString methods (no equals override)");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. value round-trip for several payload types
        Annotation str = new Annotation("segment1");
        check("segment1".equals(str.getValue()), "String value round-trip");
        Annotation num = new Annotation(42);
        check(Integer.valueOf(42).equals(num.getValue()), "Integer value round-trip");
        Object payload = new Object();
        Annotation any = new Annotation(payload);
        check(any.getValue() == payload, "arbitrary Object kept by identity");
        Annotation nil = new Annotation(null);
        check(nil.getValue() == null, "null value permitted");

        // 2. identity semantics: no equals override -> distinct instances stay distinct
        Annotation a1 = new Annotation("x");
        Annotation a2 = new Annotation("x");
        check(!a1.equals(a2), "no equals override: identity equality");
        check(a1.equals(a1), "identity equality reflexive");

        // 3. toString
        check("java.text.Annotation[value=segment1]".equals(str.toString()),
                "toString exact format");
        check("java.text.Annotation[value=42]".equals(num.toString()),
                "toString renders payload via String.valueOf");
        check("java.text.Annotation[value=null]".equals(nil.toString()),
                "toString renders null payload");

        // 4. real usage: Annotation as attribute value of an AttributedString run
        AttributedString as = new AttributedString("helloworld");
        as.addAttribute(AttributedCharacterIterator.Attribute.READING,
                new Annotation("read-0-4"), 0, 4);
        as.addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT,
                new Annotation(null), 4, 10);
        AttributedCharacterIterator it = as.getIterator();
        Object v0 = it.getAttribute(AttributedCharacterIterator.Attribute.READING);
        check(v0 instanceof Annotation && "read-0-4".equals(((Annotation) v0).getValue()),
                "attribute value survives as Annotation, getValue round-trip");
        it.setIndex(4);
        Object v4 = it.getAttribute(AttributedCharacterIterator.Attribute.READING);
        check(v4 == null, "annotation-valued attribute absent outside its run");
        Object seg = it.getAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT);
        check(seg instanceof Annotation && ((Annotation) seg).getValue() == null,
                "null-valued Annotation payload retrievable");
        // nested annotation-of-annotation (Annotation holding an Annotation)
        Annotation nested = new Annotation(new Annotation("inner"));
        check(((Annotation) nested.getValue()).getValue().equals("inner"),
                "nested Annotation payload");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testIdentitySemanticsContract() {
        Annotation s1 = new Annotation("s");
        Annotation s2 = new Annotation("s");
        check(!s1.equals(s2), "equals is identity-based (no override)");
        check(!s1.equals(null), "equals(null) false");
        check(s1.equals(s1), "reflexive");
        check(s1.hashCode() != s2.hashCode(),
              "identity hashCodes differ for equal-valued instances");
        check("java.text.Annotation[value=s]".equals(s1.toString()),
              "toString exact format");
        check("java.text.Annotation[value=null]".equals(new Annotation(null).toString()),
              "toString null value");
    }

    static void testNullValueContract() {
        check(new Annotation(null).getValue() == null, "null value permitted and readable");
        Annotation boxed = new Annotation(Integer.valueOf(7));
        check(Integer.valueOf(7).equals(boxed.getValue()), "arbitrary Object value round-trips");
    }

    static void testAsAttributeValueContract() {
        AttributedString as = new AttributedString("xyz");
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, new Annotation("r1"));
        AttributedCharacterIterator it = as.getIterator();
        Object v = it.getAttribute(AttributedCharacterIterator.Attribute.READING);
        check(v instanceof Annotation && "r1".equals(((Annotation) v).getValue()),
              "Annotation flows through AttributedString unchanged");
    }
    public static void main(String[] args) throws Exception {
        testIdentitySemanticsContract();
        testNullValueContract();
        testAsAttributeValueContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
