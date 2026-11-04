/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMoreInfoManufacturer conformance: URI attribute.
 * @run main PrinterMoreInfoManufacturerTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterMoreInfoManufacturerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterMoreInfoManufacturer v = new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x"));
        check("http://example.com/x".equals(v.getURI().toString()), "getURI");
        check(v.getCategory() == PrinterMoreInfoManufacturer.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x"))), "equals");
        check(v.hashCode() == new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x")).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
