/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.Engineering conformance: media size constants.
 * @run main EngineeringTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class EngineeringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "Engineering");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_A = c.getDeclaredField("A");
        check(java.lang.reflect.Modifier.isStatic(f_A.getModifiers()), "A is static");
        check(f_A.getType() == javax.print.attribute.standard.MediaSize.class, "A type MediaSize");
        check(f_A.get(null) != null, "A non-null");
        java.lang.reflect.Field f_B = c.getDeclaredField("B");
        check(java.lang.reflect.Modifier.isStatic(f_B.getModifiers()), "B is static");
        check(f_B.getType() == javax.print.attribute.standard.MediaSize.class, "B type MediaSize");
        check(f_B.get(null) != null, "B non-null");
        java.lang.reflect.Field f_C = c.getDeclaredField("C");
        check(java.lang.reflect.Modifier.isStatic(f_C.getModifiers()), "C is static");
        check(f_C.getType() == javax.print.attribute.standard.MediaSize.class, "C type MediaSize");
        check(f_C.get(null) != null, "C non-null");
        java.lang.reflect.Field f_D = c.getDeclaredField("D");
        check(java.lang.reflect.Modifier.isStatic(f_D.getModifiers()), "D is static");
        check(f_D.getType() == javax.print.attribute.standard.MediaSize.class, "D type MediaSize");
        check(f_D.get(null) != null, "D non-null");
        java.lang.reflect.Field f_E = c.getDeclaredField("E");
        check(java.lang.reflect.Modifier.isStatic(f_E.getModifiers()), "E is static");
        check(f_E.getType() == javax.print.attribute.standard.MediaSize.class, "E type MediaSize");
        check(f_E.get(null) != null, "E non-null");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.Engineering.A.getX(MediaSize.MM) == 215.9f && MediaSize.Engineering.A.getY(MediaSize.MM) == 279.4f, "Engineering.A 215.9x279.4 mm");
        check(MediaSize.Engineering.B.getX(MediaSize.MM) == 279.4f && MediaSize.Engineering.B.getY(MediaSize.MM) == 431.8f, "Engineering.B 279.4x431.8 mm");
        check(MediaSize.Engineering.C.getX(MediaSize.MM) == 431.8f && MediaSize.Engineering.C.getY(MediaSize.MM) == 558.8f, "Engineering.C 431.8x558.8 mm");
        check(MediaSize.Engineering.D.getX(MediaSize.MM) == 558.8f && MediaSize.Engineering.D.getY(MediaSize.MM) == 863.6f, "Engineering.D 558.8x863.6 mm");
        check(MediaSize.Engineering.E.getX(MediaSize.MM) == 863.6f && MediaSize.Engineering.E.getY(MediaSize.MM) == 1117.6f, "Engineering.E 863.6x1117.6 mm");
        // at most 1/100 mm rounding tolerance on inch-derived conversions
        check(Math.abs(MediaSize.NA.LETTER.getX(MediaSize.INCH) - 8.5f) < 1e-5f, "NA.LETTER inch");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
