/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.Other conformance: media size constants.
 * @run main OtherTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class OtherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "Other");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_EXECUTIVE = c.getDeclaredField("EXECUTIVE");
        check(java.lang.reflect.Modifier.isStatic(f_EXECUTIVE.getModifiers()), "EXECUTIVE is static");
        check(f_EXECUTIVE.getType() == javax.print.attribute.standard.MediaSize.class, "EXECUTIVE type MediaSize");
        check(f_EXECUTIVE.get(null) != null, "EXECUTIVE non-null");
        java.lang.reflect.Field f_LEDGER = c.getDeclaredField("LEDGER");
        check(java.lang.reflect.Modifier.isStatic(f_LEDGER.getModifiers()), "LEDGER is static");
        check(f_LEDGER.getType() == javax.print.attribute.standard.MediaSize.class, "LEDGER type MediaSize");
        check(f_LEDGER.get(null) != null, "LEDGER non-null");
        java.lang.reflect.Field f_TABLOID = c.getDeclaredField("TABLOID");
        check(java.lang.reflect.Modifier.isStatic(f_TABLOID.getModifiers()), "TABLOID is static");
        check(f_TABLOID.getType() == javax.print.attribute.standard.MediaSize.class, "TABLOID type MediaSize");
        check(f_TABLOID.get(null) != null, "TABLOID non-null");
        java.lang.reflect.Field f_INVOICE = c.getDeclaredField("INVOICE");
        check(java.lang.reflect.Modifier.isStatic(f_INVOICE.getModifiers()), "INVOICE is static");
        check(f_INVOICE.getType() == javax.print.attribute.standard.MediaSize.class, "INVOICE type MediaSize");
        check(f_INVOICE.get(null) != null, "INVOICE non-null");
        java.lang.reflect.Field f_FOLIO = c.getDeclaredField("FOLIO");
        check(java.lang.reflect.Modifier.isStatic(f_FOLIO.getModifiers()), "FOLIO is static");
        check(f_FOLIO.getType() == javax.print.attribute.standard.MediaSize.class, "FOLIO type MediaSize");
        check(f_FOLIO.get(null) != null, "FOLIO non-null");
        java.lang.reflect.Field f_QUARTO = c.getDeclaredField("QUARTO");
        check(java.lang.reflect.Modifier.isStatic(f_QUARTO.getModifiers()), "QUARTO is static");
        check(f_QUARTO.getType() == javax.print.attribute.standard.MediaSize.class, "QUARTO type MediaSize");
        check(f_QUARTO.get(null) != null, "QUARTO non-null");
        java.lang.reflect.Field f_ITALY_ENVELOPE = c.getDeclaredField("ITALY_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_ITALY_ENVELOPE.getModifiers()), "ITALY_ENVELOPE is static");
        check(f_ITALY_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "ITALY_ENVELOPE type MediaSize");
        check(f_ITALY_ENVELOPE.get(null) != null, "ITALY_ENVELOPE non-null");
        java.lang.reflect.Field f_MONARCH_ENVELOPE = c.getDeclaredField("MONARCH_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_MONARCH_ENVELOPE.getModifiers()), "MONARCH_ENVELOPE is static");
        check(f_MONARCH_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "MONARCH_ENVELOPE type MediaSize");
        check(f_MONARCH_ENVELOPE.get(null) != null, "MONARCH_ENVELOPE non-null");
        java.lang.reflect.Field f_PERSONAL_ENVELOPE = c.getDeclaredField("PERSONAL_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_PERSONAL_ENVELOPE.getModifiers()), "PERSONAL_ENVELOPE is static");
        check(f_PERSONAL_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "PERSONAL_ENVELOPE type MediaSize");
        check(f_PERSONAL_ENVELOPE.get(null) != null, "PERSONAL_ENVELOPE non-null");
        java.lang.reflect.Field f_JAPANESE_POSTCARD = c.getDeclaredField("JAPANESE_POSTCARD");
        check(java.lang.reflect.Modifier.isStatic(f_JAPANESE_POSTCARD.getModifiers()), "JAPANESE_POSTCARD is static");
        check(f_JAPANESE_POSTCARD.getType() == javax.print.attribute.standard.MediaSize.class, "JAPANESE_POSTCARD type MediaSize");
        check(f_JAPANESE_POSTCARD.get(null) != null, "JAPANESE_POSTCARD non-null");
        java.lang.reflect.Field f_JAPANESE_DOUBLE_POSTCARD = c.getDeclaredField("JAPANESE_DOUBLE_POSTCARD");
        check(java.lang.reflect.Modifier.isStatic(f_JAPANESE_DOUBLE_POSTCARD.getModifiers()), "JAPANESE_DOUBLE_POSTCARD is static");
        check(f_JAPANESE_DOUBLE_POSTCARD.getType() == javax.print.attribute.standard.MediaSize.class, "JAPANESE_DOUBLE_POSTCARD type MediaSize");
        check(f_JAPANESE_DOUBLE_POSTCARD.get(null) != null, "JAPANESE_DOUBLE_POSTCARD non-null");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.Other.EXECUTIVE.getX(MediaSize.MM) == 184.15f && MediaSize.Other.EXECUTIVE.getY(MediaSize.MM) == 266.7f, "Other.EXECUTIVE 184.15x266.7 mm");
        check(MediaSize.Other.LEDGER.getX(MediaSize.MM) == 279.4f && MediaSize.Other.LEDGER.getY(MediaSize.MM) == 431.8f, "Other.LEDGER 279.4x431.8 mm");
        check(MediaSize.Other.TABLOID.getX(MediaSize.MM) == 279.4f && MediaSize.Other.TABLOID.getY(MediaSize.MM) == 431.8f, "Other.TABLOID 279.4x431.8 mm");
        check(MediaSize.Other.QUARTO.getX(MediaSize.MM) == 215.9f && MediaSize.Other.QUARTO.getY(MediaSize.MM) == 275.082f, "Other.QUARTO 215.9x275.082 mm");
        check(MediaSize.Other.FOLIO.getX(MediaSize.MM) == 215.9f && MediaSize.Other.FOLIO.getY(MediaSize.MM) == 330.2f, "Other.FOLIO 215.9x330.2 mm");
        check(MediaSize.Other.INVOICE.getX(MediaSize.MM) == 139.7f && MediaSize.Other.INVOICE.getY(MediaSize.MM) == 215.9f, "Other.INVOICE 139.7x215.9 mm");
        check(MediaSize.Other.ITALY_ENVELOPE.getX(MediaSize.MM) == 110.0f && MediaSize.Other.ITALY_ENVELOPE.getY(MediaSize.MM) == 230.0f, "Other.ITALY_ENVELOPE 110.0x230.0 mm");
        check(MediaSize.Other.MONARCH_ENVELOPE.getX(MediaSize.MM) == 98.298f && MediaSize.Other.MONARCH_ENVELOPE.getY(MediaSize.MM) == 190.5f, "Other.MONARCH_ENVELOPE 98.298x190.5 mm");
        check(MediaSize.Other.PERSONAL_ENVELOPE.getX(MediaSize.MM) == 92.075f && MediaSize.Other.PERSONAL_ENVELOPE.getY(MediaSize.MM) == 165.1f, "Other.PERSONAL_ENVELOPE 92.075x165.1 mm");
        check(MediaSize.Other.JAPANESE_POSTCARD.getX(MediaSize.MM) == 100.0f && MediaSize.Other.JAPANESE_POSTCARD.getY(MediaSize.MM) == 148.0f, "Other.JAPANESE_POSTCARD 100.0x148.0 mm");
        check(MediaSize.Other.JAPANESE_DOUBLE_POSTCARD.getX(MediaSize.MM) == 148.0f && MediaSize.Other.JAPANESE_DOUBLE_POSTCARD.getY(MediaSize.MM) == 200.0f, "Other.JAPANESE_DOUBLE_POSTCARD 148.0x200.0 mm");
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
