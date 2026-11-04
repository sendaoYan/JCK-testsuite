/*
 * @test
 * @summary javax.management.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.Attribute");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getName"), "has getName");
        check(hasMethod(c, "getValue"), "has getValue");
        check(hasMethod(c, "equals"), "has equals");
        check(hasMethod(c, "hashCode"), "has hashCode");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Attribute a = new Attribute("name", "value");
        check("name".equals(a.getName()), "getName");
        check("value".equals(a.getValue()), "getValue");

        Attribute same = new Attribute("name", "value");
        check(a.equals(same), "equals symmetric");
        check(same.equals(a), "equals symmetric reverse");
        check(a.hashCode() == same.hashCode(), "equal hashCode");

        Attribute diffName = new Attribute("other", "value");
        Attribute diffValue = new Attribute("name", "other");
        check(!a.equals(diffName), "different name not equal");
        check(!a.equals(diffValue), "different value not equal");
        check(!a.equals(null), "not equal null");
        check(!a.equals("name"), "not equal non-Attribute");

        Attribute nullValue = new Attribute("name", null);
        check(nullValue.getValue() == null, "null value stored");
        check(nullValue.equals(new Attribute("name", null)), "null value equals");
        check(!nullValue.equals(a), "null vs non-null value not equal");

        Attribute nullName = null;
        boolean threwNull = false;
        try { nullName = new Attribute(null, "v"); } catch (RuntimeOperationsException ex) { threwNull = true; }
        check(threwNull, "null name throws RuntimeOperationsException");

        String s = a.toString();
        check(s.contains("name") && s.contains("value"), "toString contains name and value");

        // equals contract: reflexive and transitive
        Attribute a2 = new Attribute("name", "value");
        check(a.equals(a) && a.equals(a2) && a2.equals(a), "equals reflexive/transitive");
        check(a.equals(new Attribute("name", "value")), "new equal instance");
    }


    @SuppressWarnings("unchecked")
    static <T> T deepRoundTripValue(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (2026-11): null-name contract / serialVersionUID / serialization matrix ----

    static void testDeepNullNameContract() {
        try {
            new Attribute(null, "v");
            check(false, "Attribute(null, value) must throw");
        } catch (javax.management.RuntimeOperationsException e) {
            check(e.getTargetException() instanceof IllegalArgumentException, "wraps IllegalArgumentException");
        }
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(Attribute.class).getSerialVersionUID() == 2484220110589082382L,
                "serialVersionUID constant");
        Attribute a7 = new Attribute("N", Integer.valueOf(7));
        Attribute aS = new Attribute("S", "str");
        Attribute aNull = new Attribute("Null", null);
        Attribute b7 = deepRoundTripValue(a7);
        Attribute bS = deepRoundTripValue(aS);
        Attribute bNull = deepRoundTripValue(aNull);
        check(b7.equals(a7) && a7.equals(b7), "round-trip Integer value equal");
        check("N".equals(b7.getName()) && Integer.valueOf(7).equals(b7.getValue()), "round-trip Integer fields");
        check(bS.equals(aS) && "str".equals(bS.getValue()), "round-trip String value");
        check(bNull.equals(aNull) && bNull.getValue() == null, "round-trip null value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepNullNameContract();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
