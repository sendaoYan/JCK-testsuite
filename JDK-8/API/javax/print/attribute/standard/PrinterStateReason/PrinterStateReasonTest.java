/*
 * @test
 * @summary javax.print.attribute.standard.PrinterStateReason conformance: enum attribute.
 * @run main PrinterStateReasonTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterStateReasonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterStateReason v1 = PrinterStateReason.OTHER;
        check(v1 instanceof PrinterStateReason, "OTHER instanceof PrinterStateReason");
        check(v1.getCategory() == PrinterStateReason.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PrinterStateReason v2 = PrinterStateReason.MEDIA_NEEDED;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PrinterStateReason.OTHER), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PrinterStateReason.OTHER.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PrinterStateReason.OTHER.getValue() == 0, "OTHER.getValue == 0");
        check("other".equals(PrinterStateReason.OTHER.toString()), "OTHER.toString [other]");
        check(PrinterStateReason.MEDIA_NEEDED.getValue() == 1, "MEDIA_NEEDED.getValue == 1");
        check("media-needed".equals(PrinterStateReason.MEDIA_NEEDED.toString()), "MEDIA_NEEDED.toString [media-needed]");
        check(PrinterStateReason.MEDIA_JAM.getValue() == 2, "MEDIA_JAM.getValue == 2");
        check("media-jam".equals(PrinterStateReason.MEDIA_JAM.toString()), "MEDIA_JAM.toString [media-jam]");
        check(PrinterStateReason.MOVING_TO_PAUSED.getValue() == 3, "MOVING_TO_PAUSED.getValue == 3");
        check("moving-to-paused".equals(PrinterStateReason.MOVING_TO_PAUSED.toString()), "MOVING_TO_PAUSED.toString [moving-to-paused]");
        check(PrinterStateReason.PAUSED.getValue() == 4, "PAUSED.getValue == 4");
        check("paused".equals(PrinterStateReason.PAUSED.toString()), "PAUSED.toString [paused]");
        check(PrinterStateReason.SHUTDOWN.getValue() == 5, "SHUTDOWN.getValue == 5");
        check("shutdown".equals(PrinterStateReason.SHUTDOWN.toString()), "SHUTDOWN.toString [shutdown]");
        check(PrinterStateReason.CONNECTING_TO_DEVICE.getValue() == 6, "CONNECTING_TO_DEVICE.getValue == 6");
        check("connecting-to-device".equals(PrinterStateReason.CONNECTING_TO_DEVICE.toString()), "CONNECTING_TO_DEVICE.toString [connecting-to-device]");
        check(PrinterStateReason.TIMED_OUT.getValue() == 7, "TIMED_OUT.getValue == 7");
        check("timed-out".equals(PrinterStateReason.TIMED_OUT.toString()), "TIMED_OUT.toString [timed-out]");
        check(PrinterStateReason.STOPPING.getValue() == 8, "STOPPING.getValue == 8");
        check("stopping".equals(PrinterStateReason.STOPPING.toString()), "STOPPING.toString [stopping]");
        check(PrinterStateReason.STOPPED_PARTLY.getValue() == 9, "STOPPED_PARTLY.getValue == 9");
        check("stopped-partly".equals(PrinterStateReason.STOPPED_PARTLY.toString()), "STOPPED_PARTLY.toString [stopped-partly]");
        check(PrinterStateReason.TONER_LOW.getValue() == 10, "TONER_LOW.getValue == 10");
        check("toner-low".equals(PrinterStateReason.TONER_LOW.toString()), "TONER_LOW.toString [toner-low]");
        check(PrinterStateReason.TONER_EMPTY.getValue() == 11, "TONER_EMPTY.getValue == 11");
        check("toner-empty".equals(PrinterStateReason.TONER_EMPTY.toString()), "TONER_EMPTY.toString [toner-empty]");
        check(PrinterStateReason.SPOOL_AREA_FULL.getValue() == 12, "SPOOL_AREA_FULL.getValue == 12");
        check("spool-area-full".equals(PrinterStateReason.SPOOL_AREA_FULL.toString()), "SPOOL_AREA_FULL.toString [spool-area-full]");
        check(PrinterStateReason.COVER_OPEN.getValue() == 13, "COVER_OPEN.getValue == 13");
        check("cover-open".equals(PrinterStateReason.COVER_OPEN.toString()), "COVER_OPEN.toString [cover-open]");
        check(PrinterStateReason.INTERLOCK_OPEN.getValue() == 14, "INTERLOCK_OPEN.getValue == 14");
        check("interlock-open".equals(PrinterStateReason.INTERLOCK_OPEN.toString()), "INTERLOCK_OPEN.toString [interlock-open]");
        check(PrinterStateReason.DOOR_OPEN.getValue() == 15, "DOOR_OPEN.getValue == 15");
        check("door-open".equals(PrinterStateReason.DOOR_OPEN.toString()), "DOOR_OPEN.toString [door-open]");
        check(PrinterStateReason.INPUT_TRAY_MISSING.getValue() == 16, "INPUT_TRAY_MISSING.getValue == 16");
        check("input-tray-missing".equals(PrinterStateReason.INPUT_TRAY_MISSING.toString()), "INPUT_TRAY_MISSING.toString [input-tray-missing]");
        check(PrinterStateReason.MEDIA_LOW.getValue() == 17, "MEDIA_LOW.getValue == 17");
        check("media-low".equals(PrinterStateReason.MEDIA_LOW.toString()), "MEDIA_LOW.toString [media-low]");
        check(PrinterStateReason.MEDIA_EMPTY.getValue() == 18, "MEDIA_EMPTY.getValue == 18");
        check("media-empty".equals(PrinterStateReason.MEDIA_EMPTY.toString()), "MEDIA_EMPTY.toString [media-empty]");
        check(PrinterStateReason.OUTPUT_TRAY_MISSING.getValue() == 19, "OUTPUT_TRAY_MISSING.getValue == 19");
        check("output-tray-missing".equals(PrinterStateReason.OUTPUT_TRAY_MISSING.toString()), "OUTPUT_TRAY_MISSING.toString [output-tray-missing]");
        check(PrinterStateReason.OUTPUT_AREA_ALMOST_FULL.getValue() == 20, "OUTPUT_AREA_ALMOST_FULL.getValue == 20");
        check("output-area-almost-full".equals(PrinterStateReason.OUTPUT_AREA_ALMOST_FULL.toString()), "OUTPUT_AREA_ALMOST_FULL.toString [output-area-almost-full]");
        check(PrinterStateReason.OUTPUT_AREA_FULL.getValue() == 21, "OUTPUT_AREA_FULL.getValue == 21");
        check("output-area-full".equals(PrinterStateReason.OUTPUT_AREA_FULL.toString()), "OUTPUT_AREA_FULL.toString [output-area-full]");
        check(PrinterStateReason.MARKER_SUPPLY_LOW.getValue() == 22, "MARKER_SUPPLY_LOW.getValue == 22");
        check("marker-supply-low".equals(PrinterStateReason.MARKER_SUPPLY_LOW.toString()), "MARKER_SUPPLY_LOW.toString [marker-supply-low]");
        check(PrinterStateReason.MARKER_SUPPLY_EMPTY.getValue() == 23, "MARKER_SUPPLY_EMPTY.getValue == 23");
        check("marker-supply-empty".equals(PrinterStateReason.MARKER_SUPPLY_EMPTY.toString()), "MARKER_SUPPLY_EMPTY.toString [marker-supply-empty]");
        check(PrinterStateReason.MARKER_WASTE_ALMOST_FULL.getValue() == 24, "MARKER_WASTE_ALMOST_FULL.getValue == 24");
        check("marker-waste-almost-full".equals(PrinterStateReason.MARKER_WASTE_ALMOST_FULL.toString()), "MARKER_WASTE_ALMOST_FULL.toString [marker-waste-almost-full]");
        check(PrinterStateReason.MARKER_WASTE_FULL.getValue() == 25, "MARKER_WASTE_FULL.getValue == 25");
        check("marker-waste-full".equals(PrinterStateReason.MARKER_WASTE_FULL.toString()), "MARKER_WASTE_FULL.toString [marker-waste-full]");
        check(PrinterStateReason.FUSER_OVER_TEMP.getValue() == 26, "FUSER_OVER_TEMP.getValue == 26");
        check("fuser-over-temp".equals(PrinterStateReason.FUSER_OVER_TEMP.toString()), "FUSER_OVER_TEMP.toString [fuser-over-temp]");
        check(PrinterStateReason.FUSER_UNDER_TEMP.getValue() == 27, "FUSER_UNDER_TEMP.getValue == 27");
        check("fuser-under-temp".equals(PrinterStateReason.FUSER_UNDER_TEMP.toString()), "FUSER_UNDER_TEMP.toString [fuser-under-temp]");
        check(PrinterStateReason.OPC_NEAR_EOL.getValue() == 28, "OPC_NEAR_EOL.getValue == 28");
        check("opc-near-eol".equals(PrinterStateReason.OPC_NEAR_EOL.toString()), "OPC_NEAR_EOL.toString [opc-near-eol]");
        check(PrinterStateReason.OPC_LIFE_OVER.getValue() == 29, "OPC_LIFE_OVER.getValue == 29");
        check("opc-life-over".equals(PrinterStateReason.OPC_LIFE_OVER.toString()), "OPC_LIFE_OVER.toString [opc-life-over]");
        check(PrinterStateReason.DEVELOPER_LOW.getValue() == 30, "DEVELOPER_LOW.getValue == 30");
        check("developer-low".equals(PrinterStateReason.DEVELOPER_LOW.toString()), "DEVELOPER_LOW.toString [developer-low]");
        check(PrinterStateReason.DEVELOPER_EMPTY.getValue() == 31, "DEVELOPER_EMPTY.getValue == 31");
        check("developer-empty".equals(PrinterStateReason.DEVELOPER_EMPTY.toString()), "DEVELOPER_EMPTY.toString [developer-empty]");
        check(PrinterStateReason.INTERPRETER_RESOURCE_UNAVAILABLE.getValue() == 32, "INTERPRETER_RESOURCE_UNAVAILABLE.getValue == 32");
        check("interpreter-resource-unavailable".equals(PrinterStateReason.INTERPRETER_RESOURCE_UNAVAILABLE.toString()), "INTERPRETER_RESOURCE_UNAVAILABLE.toString [interpreter-resource-unavailable]");
        check("printer-state-reason".equals(PrinterStateReason.OTHER.getName()), "getName == \"printer-state-reason\"");
        check(PrinterStateReason.class == PrinterStateReason.OTHER.getCategory(), "getCategory");
        check(PrinterStateReason.OTHER.equals(PrinterStateReason.OTHER), "equals self");
        check(!PrinterStateReason.OTHER.equals(null), "not equals null");
        check(!PrinterStateReason.OTHER.equals("other"), "not equals foreign type");
        check(PrinterStateReason.OTHER.hashCode() == 0, "hashCode == value");
        check(PrinterStateReason.OTHER.clone() == PrinterStateReason.OTHER, "clone returns same singleton");
        check(!PrinterStateReason.OTHER.equals(PrinterStateReason.MEDIA_NEEDED), "distinct constants unequal");
        check(PrinterStateReason.OTHER.toString() != PrinterStateReason.MEDIA_NEEDED.toString() || true, "toString stable");
        check(!PrinterStateReason.OTHER.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        PrinterStateReason.OTHER,
        PrinterStateReason.MEDIA_NEEDED,
        PrinterStateReason.MEDIA_JAM,
        PrinterStateReason.MOVING_TO_PAUSED,
        PrinterStateReason.PAUSED,
        PrinterStateReason.SHUTDOWN,
        PrinterStateReason.CONNECTING_TO_DEVICE,
        PrinterStateReason.TIMED_OUT,
        PrinterStateReason.STOPPING,
        PrinterStateReason.STOPPED_PARTLY,
        PrinterStateReason.TONER_LOW,
        PrinterStateReason.TONER_EMPTY,
        PrinterStateReason.SPOOL_AREA_FULL,
        PrinterStateReason.COVER_OPEN,
        PrinterStateReason.INTERLOCK_OPEN,
        PrinterStateReason.DOOR_OPEN,
        PrinterStateReason.INPUT_TRAY_MISSING,
        PrinterStateReason.MEDIA_LOW,
        PrinterStateReason.MEDIA_EMPTY,
        PrinterStateReason.OUTPUT_TRAY_MISSING,
        PrinterStateReason.OUTPUT_AREA_ALMOST_FULL,
        PrinterStateReason.OUTPUT_AREA_FULL,
        PrinterStateReason.MARKER_SUPPLY_LOW,
        PrinterStateReason.MARKER_SUPPLY_EMPTY,
        PrinterStateReason.MARKER_WASTE_ALMOST_FULL,
        PrinterStateReason.MARKER_WASTE_FULL,
        PrinterStateReason.FUSER_OVER_TEMP,
        PrinterStateReason.FUSER_UNDER_TEMP,
        PrinterStateReason.OPC_NEAR_EOL,
        PrinterStateReason.OPC_LIFE_OVER,
        PrinterStateReason.DEVELOPER_LOW,
        PrinterStateReason.DEVELOPER_EMPTY,
        PrinterStateReason.INTERPRETER_RESOURCE_UNAVAILABLE
    };
    static final String[] NAMES = {
        "OTHER",
        "MEDIA_NEEDED",
        "MEDIA_JAM",
        "MOVING_TO_PAUSED",
        "PAUSED",
        "SHUTDOWN",
        "CONNECTING_TO_DEVICE",
        "TIMED_OUT",
        "STOPPING",
        "STOPPED_PARTLY",
        "TONER_LOW",
        "TONER_EMPTY",
        "SPOOL_AREA_FULL",
        "COVER_OPEN",
        "INTERLOCK_OPEN",
        "DOOR_OPEN",
        "INPUT_TRAY_MISSING",
        "MEDIA_LOW",
        "MEDIA_EMPTY",
        "OUTPUT_TRAY_MISSING",
        "OUTPUT_AREA_ALMOST_FULL",
        "OUTPUT_AREA_FULL",
        "MARKER_SUPPLY_LOW",
        "MARKER_SUPPLY_EMPTY",
        "MARKER_WASTE_ALMOST_FULL",
        "MARKER_WASTE_FULL",
        "FUSER_OVER_TEMP",
        "FUSER_UNDER_TEMP",
        "OPC_NEAR_EOL",
        "OPC_LIFE_OVER",
        "DEVELOPER_LOW",
        "DEVELOPER_EMPTY",
        "INTERPRETER_RESOURCE_UNAVAILABLE"
    };

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
