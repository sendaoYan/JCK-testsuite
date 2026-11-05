/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.XmlAdapter conformance: abstract class hierarchy and methods.
 * @run main XmlAdapterTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class XmlAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.adapters.XmlAdapter");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("unmarshal"), "has method unmarshal");
        check(names.contains("marshal"), "has method marshal");
        testClassContract();
        testSubclassing();
        System.out.println("PASS (" + passed + " assertions)");
    }

    /** String <-> Integer adapter used to exercise the abstract contract. */
    static class IntAdapter extends XmlAdapter<String, Integer> {
        @Override
        public Integer unmarshal(String v) {
            return v == null ? null : Integer.valueOf(Integer.parseInt(v.trim()));
        }

        @Override
        public String marshal(Integer v) {
            return v == null ? null : v.toString();
        }
    }

    /** Adapter whose unmarshal throws, proving signatures declare throws Exception. */
    static class FailingAdapter extends XmlAdapter<String, String> {
        @Override
        public String unmarshal(String v) throws Exception {
            throw new java.io.IOException("boom");
        }

        @Override
        public String marshal(String v) throws Exception {
            throw new java.io.IOException("marshal-boom");
        }
    }

    static void testClassContract() throws Exception {
        Class<XmlAdapter> c = XmlAdapter.class;
        check(c.getSuperclass() == Object.class, "direct subclass of Object");
        check(c.isInterface() == false && !c.isAnnotation() && !c.isEnum(), "plain abstract class");
        TypeVariable<?>[] tps = c.getTypeParameters();
        check(tps.length == 2, "two type parameters ValueType, BoundType");
        check("ValueType".equals(tps[0].getName()), "first type parameter named ValueType (xml representation)");
        check("BoundType".equals(tps[1].getName()), "second type parameter named BoundType (java representation)");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 2, "exactly 2 declared methods");
        for (Method m : declared) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " is abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " is public");
            check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == Exception.class,
                    m.getName() + " declares throws Exception");
            check(m.getParameterTypes().length == 1, m.getName() + " takes exactly 1 parameter");
        }
        Method un = c.getDeclaredMethod("unmarshal", Object.class);
        check(un.getReturnType() == Object.class, "unmarshal erased signature (ValueType)->BoundType");
        Method ma = c.getDeclaredMethod("marshal", Object.class);
        check(ma.getReturnType() == Object.class, "marshal erased signature (BoundType)->ValueType");
        // XmlAdapter has a single protected constructor; foreign-package reflection is blocked
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared constructor");
        check(Modifier.isProtected(ctors[0].getModifiers()), "constructor is protected (subclass use only)");
        try {
            Object o = XmlAdapter.class.newInstance();
            check(false, "cannot be instantiated reflectively from a foreign package: " + o);
        } catch (IllegalAccessException e) {
            check(true, "newInstance from foreign package -> IllegalAccessException (protected ctor)");
        } catch (InstantiationException e) {
            check(true, "newInstance on abstract XmlAdapter -> InstantiationException");
        }
    }

    static void testSubclassing() throws Exception {
        IntAdapter ad = new IntAdapter();
        check(ad instanceof XmlAdapter, "subclass instance of XmlAdapter");
        check(Integer.valueOf(42).equals(ad.unmarshal("42")), "custom unmarshal parse");
        check(Integer.valueOf(-7).equals(ad.unmarshal(" -7 ")), "custom unmarshal trims");
        check("42".equals(ad.marshal(42)), "custom marshal print");
        check(ad.unmarshal(null) == null, "custom null handling in unmarshal");
        check(ad.marshal(null) == null, "custom null handling in marshal");
        check("123".equals(ad.marshal(ad.unmarshal("123"))), "round-trip through both directions");
        // typed as the generic supertype
        XmlAdapter<String, Integer> gen = ad;
        check(Integer.valueOf(9).equals(gen.unmarshal("9")), "bridge unmarshal via supertype reference");
        check("9".equals(gen.marshal(9)), "bridge marshal via supertype reference");
        // exceptions propagate through the declared throws Exception
        FailingAdapter fa = new FailingAdapter();
        try {
            fa.unmarshal("x");
            check(false, "failing unmarshal must throw");
        } catch (java.io.IOException e) {
            check("boom".equals(e.getMessage()), "subclass exception type propagates unchanged");
        }
        try {
            fa.marshal("x");
            check(false, "failing marshal must throw");
        } catch (java.io.IOException e) {
            check("marshal-boom".equals(e.getMessage()), "marshal exception propagates");
        }
        // RI adapters are themselves XmlAdapter subclasses
        check(new HexBinaryAdapter() instanceof XmlAdapter, "HexBinaryAdapter is an XmlAdapter");
    }
}
