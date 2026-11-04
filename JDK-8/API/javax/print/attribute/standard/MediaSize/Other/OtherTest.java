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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
