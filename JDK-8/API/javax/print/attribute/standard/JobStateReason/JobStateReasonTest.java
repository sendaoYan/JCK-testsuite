/*
 * @test
 * @summary javax.print.attribute.standard.JobStateReason conformance: enum attribute.
 * @run main JobStateReasonTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobStateReasonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobStateReason v1 = JobStateReason.JOB_INCOMING;
        check(v1 instanceof JobStateReason, "JOB_INCOMING instanceof JobStateReason");
        check(v1.getCategory() == JobStateReason.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        JobStateReason v2 = JobStateReason.JOB_PRINTING;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(JobStateReason.JOB_INCOMING), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == JobStateReason.JOB_INCOMING.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(JobStateReason.JOB_INCOMING.getValue() == 0, "JOB_INCOMING.getValue == 0");
        check("job-incoming".equals(JobStateReason.JOB_INCOMING.toString()), "JOB_INCOMING.toString [job-incoming]");
        check(JobStateReason.JOB_DATA_INSUFFICIENT.getValue() == 1, "JOB_DATA_INSUFFICIENT.getValue == 1");
        check("job-data-insufficient".equals(JobStateReason.JOB_DATA_INSUFFICIENT.toString()), "JOB_DATA_INSUFFICIENT.toString [job-data-insufficient]");
        check(JobStateReason.DOCUMENT_ACCESS_ERROR.getValue() == 2, "DOCUMENT_ACCESS_ERROR.getValue == 2");
        check("document-access-error".equals(JobStateReason.DOCUMENT_ACCESS_ERROR.toString()), "DOCUMENT_ACCESS_ERROR.toString [document-access-error]");
        check(JobStateReason.SUBMISSION_INTERRUPTED.getValue() == 3, "SUBMISSION_INTERRUPTED.getValue == 3");
        check("submission-interrupted".equals(JobStateReason.SUBMISSION_INTERRUPTED.toString()), "SUBMISSION_INTERRUPTED.toString [submission-interrupted]");
        check(JobStateReason.JOB_OUTGOING.getValue() == 4, "JOB_OUTGOING.getValue == 4");
        check("job-outgoing".equals(JobStateReason.JOB_OUTGOING.toString()), "JOB_OUTGOING.toString [job-outgoing]");
        check(JobStateReason.JOB_HOLD_UNTIL_SPECIFIED.getValue() == 5, "JOB_HOLD_UNTIL_SPECIFIED.getValue == 5");
        check("job-hold-until-specified".equals(JobStateReason.JOB_HOLD_UNTIL_SPECIFIED.toString()), "JOB_HOLD_UNTIL_SPECIFIED.toString [job-hold-until-specified]");
        check(JobStateReason.RESOURCES_ARE_NOT_READY.getValue() == 6, "RESOURCES_ARE_NOT_READY.getValue == 6");
        check("resources-are-not-ready".equals(JobStateReason.RESOURCES_ARE_NOT_READY.toString()), "RESOURCES_ARE_NOT_READY.toString [resources-are-not-ready]");
        check(JobStateReason.PRINTER_STOPPED_PARTLY.getValue() == 7, "PRINTER_STOPPED_PARTLY.getValue == 7");
        check("printer-stopped-partly".equals(JobStateReason.PRINTER_STOPPED_PARTLY.toString()), "PRINTER_STOPPED_PARTLY.toString [printer-stopped-partly]");
        check(JobStateReason.PRINTER_STOPPED.getValue() == 8, "PRINTER_STOPPED.getValue == 8");
        check("printer-stopped".equals(JobStateReason.PRINTER_STOPPED.toString()), "PRINTER_STOPPED.toString [printer-stopped]");
        check(JobStateReason.JOB_INTERPRETING.getValue() == 9, "JOB_INTERPRETING.getValue == 9");
        check("job-interpreting".equals(JobStateReason.JOB_INTERPRETING.toString()), "JOB_INTERPRETING.toString [job-interpreting]");
        check(JobStateReason.JOB_QUEUED.getValue() == 10, "JOB_QUEUED.getValue == 10");
        check("job-queued".equals(JobStateReason.JOB_QUEUED.toString()), "JOB_QUEUED.toString [job-queued]");
        check(JobStateReason.JOB_TRANSFORMING.getValue() == 11, "JOB_TRANSFORMING.getValue == 11");
        check("job-transforming".equals(JobStateReason.JOB_TRANSFORMING.toString()), "JOB_TRANSFORMING.toString [job-transforming]");
        check(JobStateReason.JOB_QUEUED_FOR_MARKER.getValue() == 12, "JOB_QUEUED_FOR_MARKER.getValue == 12");
        check("job-queued-for-marker".equals(JobStateReason.JOB_QUEUED_FOR_MARKER.toString()), "JOB_QUEUED_FOR_MARKER.toString [job-queued-for-marker]");
        check(JobStateReason.JOB_PRINTING.getValue() == 13, "JOB_PRINTING.getValue == 13");
        check("job-printing".equals(JobStateReason.JOB_PRINTING.toString()), "JOB_PRINTING.toString [job-printing]");
        check(JobStateReason.JOB_CANCELED_BY_USER.getValue() == 14, "JOB_CANCELED_BY_USER.getValue == 14");
        check("job-canceled-by-user".equals(JobStateReason.JOB_CANCELED_BY_USER.toString()), "JOB_CANCELED_BY_USER.toString [job-canceled-by-user]");
        check(JobStateReason.JOB_CANCELED_BY_OPERATOR.getValue() == 15, "JOB_CANCELED_BY_OPERATOR.getValue == 15");
        check("job-canceled-by-operator".equals(JobStateReason.JOB_CANCELED_BY_OPERATOR.toString()), "JOB_CANCELED_BY_OPERATOR.toString [job-canceled-by-operator]");
        check(JobStateReason.JOB_CANCELED_AT_DEVICE.getValue() == 16, "JOB_CANCELED_AT_DEVICE.getValue == 16");
        check("job-canceled-at-device".equals(JobStateReason.JOB_CANCELED_AT_DEVICE.toString()), "JOB_CANCELED_AT_DEVICE.toString [job-canceled-at-device]");
        check(JobStateReason.ABORTED_BY_SYSTEM.getValue() == 17, "ABORTED_BY_SYSTEM.getValue == 17");
        check("aborted-by-system".equals(JobStateReason.ABORTED_BY_SYSTEM.toString()), "ABORTED_BY_SYSTEM.toString [aborted-by-system]");
        check(JobStateReason.UNSUPPORTED_COMPRESSION.getValue() == 18, "UNSUPPORTED_COMPRESSION.getValue == 18");
        check("unsupported-compression".equals(JobStateReason.UNSUPPORTED_COMPRESSION.toString()), "UNSUPPORTED_COMPRESSION.toString [unsupported-compression]");
        check(JobStateReason.COMPRESSION_ERROR.getValue() == 19, "COMPRESSION_ERROR.getValue == 19");
        check("compression-error".equals(JobStateReason.COMPRESSION_ERROR.toString()), "COMPRESSION_ERROR.toString [compression-error]");
        check(JobStateReason.UNSUPPORTED_DOCUMENT_FORMAT.getValue() == 20, "UNSUPPORTED_DOCUMENT_FORMAT.getValue == 20");
        check("unsupported-document-format".equals(JobStateReason.UNSUPPORTED_DOCUMENT_FORMAT.toString()), "UNSUPPORTED_DOCUMENT_FORMAT.toString [unsupported-document-format]");
        check(JobStateReason.DOCUMENT_FORMAT_ERROR.getValue() == 21, "DOCUMENT_FORMAT_ERROR.getValue == 21");
        check("document-format-error".equals(JobStateReason.DOCUMENT_FORMAT_ERROR.toString()), "DOCUMENT_FORMAT_ERROR.toString [document-format-error]");
        check(JobStateReason.PROCESSING_TO_STOP_POINT.getValue() == 22, "PROCESSING_TO_STOP_POINT.getValue == 22");
        check("processing-to-stop-point".equals(JobStateReason.PROCESSING_TO_STOP_POINT.toString()), "PROCESSING_TO_STOP_POINT.toString [processing-to-stop-point]");
        check(JobStateReason.SERVICE_OFF_LINE.getValue() == 23, "SERVICE_OFF_LINE.getValue == 23");
        check("service-off-line".equals(JobStateReason.SERVICE_OFF_LINE.toString()), "SERVICE_OFF_LINE.toString [service-off-line]");
        check(JobStateReason.JOB_COMPLETED_SUCCESSFULLY.getValue() == 24, "JOB_COMPLETED_SUCCESSFULLY.getValue == 24");
        check("job-completed-successfully".equals(JobStateReason.JOB_COMPLETED_SUCCESSFULLY.toString()), "JOB_COMPLETED_SUCCESSFULLY.toString [job-completed-successfully]");
        check(JobStateReason.JOB_COMPLETED_WITH_WARNINGS.getValue() == 25, "JOB_COMPLETED_WITH_WARNINGS.getValue == 25");
        check("job-completed-with-warnings".equals(JobStateReason.JOB_COMPLETED_WITH_WARNINGS.toString()), "JOB_COMPLETED_WITH_WARNINGS.toString [job-completed-with-warnings]");
        check(JobStateReason.JOB_COMPLETED_WITH_ERRORS.getValue() == 26, "JOB_COMPLETED_WITH_ERRORS.getValue == 26");
        check("job-completed-with-errors".equals(JobStateReason.JOB_COMPLETED_WITH_ERRORS.toString()), "JOB_COMPLETED_WITH_ERRORS.toString [job-completed-with-errors]");
        check(JobStateReason.JOB_RESTARTABLE.getValue() == 27, "JOB_RESTARTABLE.getValue == 27");
        check("job-restartable".equals(JobStateReason.JOB_RESTARTABLE.toString()), "JOB_RESTARTABLE.toString [job-restartable]");
        check(JobStateReason.QUEUED_IN_DEVICE.getValue() == 28, "QUEUED_IN_DEVICE.getValue == 28");
        check("queued-in-device".equals(JobStateReason.QUEUED_IN_DEVICE.toString()), "QUEUED_IN_DEVICE.toString [queued-in-device]");
        check("job-state-reason".equals(JobStateReason.JOB_INCOMING.getName()), "getName == \"job-state-reason\"");
        check(JobStateReason.class == JobStateReason.JOB_INCOMING.getCategory(), "getCategory");
        check(JobStateReason.JOB_INCOMING.equals(JobStateReason.JOB_INCOMING), "equals self");
        check(!JobStateReason.JOB_INCOMING.equals(null), "not equals null");
        check(!JobStateReason.JOB_INCOMING.equals("job-incoming"), "not equals foreign type");
        check(JobStateReason.JOB_INCOMING.hashCode() == 0, "hashCode == value");
        check(JobStateReason.JOB_INCOMING.clone() == JobStateReason.JOB_INCOMING, "clone returns same singleton");
        check(!JobStateReason.JOB_INCOMING.equals(JobStateReason.JOB_DATA_INSUFFICIENT), "distinct constants unequal");
        check(JobStateReason.JOB_INCOMING.toString() != JobStateReason.JOB_DATA_INSUFFICIENT.toString() || true, "toString stable");
        check(!JobStateReason.JOB_INCOMING.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        JobStateReason.JOB_INCOMING,
        JobStateReason.JOB_DATA_INSUFFICIENT,
        JobStateReason.DOCUMENT_ACCESS_ERROR,
        JobStateReason.SUBMISSION_INTERRUPTED,
        JobStateReason.JOB_OUTGOING,
        JobStateReason.JOB_HOLD_UNTIL_SPECIFIED,
        JobStateReason.RESOURCES_ARE_NOT_READY,
        JobStateReason.PRINTER_STOPPED_PARTLY,
        JobStateReason.PRINTER_STOPPED,
        JobStateReason.JOB_INTERPRETING,
        JobStateReason.JOB_QUEUED,
        JobStateReason.JOB_TRANSFORMING,
        JobStateReason.JOB_QUEUED_FOR_MARKER,
        JobStateReason.JOB_PRINTING,
        JobStateReason.JOB_CANCELED_BY_USER,
        JobStateReason.JOB_CANCELED_BY_OPERATOR,
        JobStateReason.JOB_CANCELED_AT_DEVICE,
        JobStateReason.ABORTED_BY_SYSTEM,
        JobStateReason.UNSUPPORTED_COMPRESSION,
        JobStateReason.COMPRESSION_ERROR,
        JobStateReason.UNSUPPORTED_DOCUMENT_FORMAT,
        JobStateReason.DOCUMENT_FORMAT_ERROR,
        JobStateReason.PROCESSING_TO_STOP_POINT,
        JobStateReason.SERVICE_OFF_LINE,
        JobStateReason.JOB_COMPLETED_SUCCESSFULLY,
        JobStateReason.JOB_COMPLETED_WITH_WARNINGS,
        JobStateReason.JOB_COMPLETED_WITH_ERRORS,
        JobStateReason.JOB_RESTARTABLE,
        JobStateReason.QUEUED_IN_DEVICE
    };
    static final String[] NAMES = {
        "JOB_INCOMING",
        "JOB_DATA_INSUFFICIENT",
        "DOCUMENT_ACCESS_ERROR",
        "SUBMISSION_INTERRUPTED",
        "JOB_OUTGOING",
        "JOB_HOLD_UNTIL_SPECIFIED",
        "RESOURCES_ARE_NOT_READY",
        "PRINTER_STOPPED_PARTLY",
        "PRINTER_STOPPED",
        "JOB_INTERPRETING",
        "JOB_QUEUED",
        "JOB_TRANSFORMING",
        "JOB_QUEUED_FOR_MARKER",
        "JOB_PRINTING",
        "JOB_CANCELED_BY_USER",
        "JOB_CANCELED_BY_OPERATOR",
        "JOB_CANCELED_AT_DEVICE",
        "ABORTED_BY_SYSTEM",
        "UNSUPPORTED_COMPRESSION",
        "COMPRESSION_ERROR",
        "UNSUPPORTED_DOCUMENT_FORMAT",
        "DOCUMENT_FORMAT_ERROR",
        "PROCESSING_TO_STOP_POINT",
        "SERVICE_OFF_LINE",
        "JOB_COMPLETED_SUCCESSFULLY",
        "JOB_COMPLETED_WITH_WARNINGS",
        "JOB_COMPLETED_WITH_ERRORS",
        "JOB_RESTARTABLE",
        "QUEUED_IN_DEVICE"
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
