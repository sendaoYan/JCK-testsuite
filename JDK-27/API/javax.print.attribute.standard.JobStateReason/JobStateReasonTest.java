/*
 * @test
 * @summary javax.print.attribute.standard.JobStateReason conformance: white-box structure + black-box behavior.
 * @run main JobStateReasonTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobStateReasonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobStateReason");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(JobStateReason.JOB_INCOMING.getCategory() == JobStateReason.class, "getCategory");
        check("job-state-reason".equals(JobStateReason.JOB_INCOMING.getName()), "getName");
        check(JobStateReason.JOB_INCOMING.getValue() == 0, "JOB_INCOMING value == 0");
        check("job-incoming".equals(JobStateReason.JOB_INCOMING.toString()), "JOB_INCOMING toString");
        check(JobStateReason.JOB_DATA_INSUFFICIENT.getValue() == 1, "JOB_DATA_INSUFFICIENT value == 1");
        check("job-data-insufficient".equals(JobStateReason.JOB_DATA_INSUFFICIENT.toString()), "JOB_DATA_INSUFFICIENT toString");
        check(JobStateReason.DOCUMENT_ACCESS_ERROR.getValue() == 2, "DOCUMENT_ACCESS_ERROR value == 2");
        check("document-access-error".equals(JobStateReason.DOCUMENT_ACCESS_ERROR.toString()), "DOCUMENT_ACCESS_ERROR toString");
        check(JobStateReason.SUBMISSION_INTERRUPTED.getValue() == 3, "SUBMISSION_INTERRUPTED value == 3");
        check("submission-interrupted".equals(JobStateReason.SUBMISSION_INTERRUPTED.toString()), "SUBMISSION_INTERRUPTED toString");
        check(JobStateReason.JOB_OUTGOING.getValue() == 4, "JOB_OUTGOING value == 4");
        check("job-outgoing".equals(JobStateReason.JOB_OUTGOING.toString()), "JOB_OUTGOING toString");
        check(JobStateReason.JOB_HOLD_UNTIL_SPECIFIED.getValue() == 5, "JOB_HOLD_UNTIL_SPECIFIED value == 5");
        check("job-hold-until-specified".equals(JobStateReason.JOB_HOLD_UNTIL_SPECIFIED.toString()), "JOB_HOLD_UNTIL_SPECIFIED toString");
        check(JobStateReason.RESOURCES_ARE_NOT_READY.getValue() == 6, "RESOURCES_ARE_NOT_READY value == 6");
        check("resources-are-not-ready".equals(JobStateReason.RESOURCES_ARE_NOT_READY.toString()), "RESOURCES_ARE_NOT_READY toString");
        check(JobStateReason.PRINTER_STOPPED_PARTLY.getValue() == 7, "PRINTER_STOPPED_PARTLY value == 7");
        check("printer-stopped-partly".equals(JobStateReason.PRINTER_STOPPED_PARTLY.toString()), "PRINTER_STOPPED_PARTLY toString");
        check(JobStateReason.PRINTER_STOPPED.getValue() == 8, "PRINTER_STOPPED value == 8");
        check("printer-stopped".equals(JobStateReason.PRINTER_STOPPED.toString()), "PRINTER_STOPPED toString");
        check(JobStateReason.JOB_INTERPRETING.getValue() == 9, "JOB_INTERPRETING value == 9");
        check("job-interpreting".equals(JobStateReason.JOB_INTERPRETING.toString()), "JOB_INTERPRETING toString");
        check(JobStateReason.JOB_QUEUED.getValue() == 10, "JOB_QUEUED value == 10");
        check("job-queued".equals(JobStateReason.JOB_QUEUED.toString()), "JOB_QUEUED toString");
        check(JobStateReason.JOB_TRANSFORMING.getValue() == 11, "JOB_TRANSFORMING value == 11");
        check("job-transforming".equals(JobStateReason.JOB_TRANSFORMING.toString()), "JOB_TRANSFORMING toString");
        check(JobStateReason.JOB_QUEUED_FOR_MARKER.getValue() == 12, "JOB_QUEUED_FOR_MARKER value == 12");
        check("job-queued-for-marker".equals(JobStateReason.JOB_QUEUED_FOR_MARKER.toString()), "JOB_QUEUED_FOR_MARKER toString");
        check(JobStateReason.JOB_PRINTING.getValue() == 13, "JOB_PRINTING value == 13");
        check("job-printing".equals(JobStateReason.JOB_PRINTING.toString()), "JOB_PRINTING toString");
        check(JobStateReason.JOB_CANCELED_BY_USER.getValue() == 14, "JOB_CANCELED_BY_USER value == 14");
        check("job-canceled-by-user".equals(JobStateReason.JOB_CANCELED_BY_USER.toString()), "JOB_CANCELED_BY_USER toString");
        check(JobStateReason.JOB_CANCELED_BY_OPERATOR.getValue() == 15, "JOB_CANCELED_BY_OPERATOR value == 15");
        check("job-canceled-by-operator".equals(JobStateReason.JOB_CANCELED_BY_OPERATOR.toString()), "JOB_CANCELED_BY_OPERATOR toString");
        check(JobStateReason.JOB_CANCELED_AT_DEVICE.getValue() == 16, "JOB_CANCELED_AT_DEVICE value == 16");
        check("job-canceled-at-device".equals(JobStateReason.JOB_CANCELED_AT_DEVICE.toString()), "JOB_CANCELED_AT_DEVICE toString");
        check(JobStateReason.ABORTED_BY_SYSTEM.getValue() == 17, "ABORTED_BY_SYSTEM value == 17");
        check("aborted-by-system".equals(JobStateReason.ABORTED_BY_SYSTEM.toString()), "ABORTED_BY_SYSTEM toString");
        check(JobStateReason.UNSUPPORTED_COMPRESSION.getValue() == 18, "UNSUPPORTED_COMPRESSION value == 18");
        check("unsupported-compression".equals(JobStateReason.UNSUPPORTED_COMPRESSION.toString()), "UNSUPPORTED_COMPRESSION toString");
        check(JobStateReason.COMPRESSION_ERROR.getValue() == 19, "COMPRESSION_ERROR value == 19");
        check("compression-error".equals(JobStateReason.COMPRESSION_ERROR.toString()), "COMPRESSION_ERROR toString");
        check(JobStateReason.UNSUPPORTED_DOCUMENT_FORMAT.getValue() == 20, "UNSUPPORTED_DOCUMENT_FORMAT value == 20");
        check("unsupported-document-format".equals(JobStateReason.UNSUPPORTED_DOCUMENT_FORMAT.toString()), "UNSUPPORTED_DOCUMENT_FORMAT toString");
        check(JobStateReason.DOCUMENT_FORMAT_ERROR.getValue() == 21, "DOCUMENT_FORMAT_ERROR value == 21");
        check("document-format-error".equals(JobStateReason.DOCUMENT_FORMAT_ERROR.toString()), "DOCUMENT_FORMAT_ERROR toString");
        check(JobStateReason.PROCESSING_TO_STOP_POINT.getValue() == 22, "PROCESSING_TO_STOP_POINT value == 22");
        check("processing-to-stop-point".equals(JobStateReason.PROCESSING_TO_STOP_POINT.toString()), "PROCESSING_TO_STOP_POINT toString");
        check(JobStateReason.SERVICE_OFF_LINE.getValue() == 23, "SERVICE_OFF_LINE value == 23");
        check("service-off-line".equals(JobStateReason.SERVICE_OFF_LINE.toString()), "SERVICE_OFF_LINE toString");
        check(JobStateReason.JOB_COMPLETED_SUCCESSFULLY.getValue() == 24, "JOB_COMPLETED_SUCCESSFULLY value == 24");
        check("job-completed-successfully".equals(JobStateReason.JOB_COMPLETED_SUCCESSFULLY.toString()), "JOB_COMPLETED_SUCCESSFULLY toString");
        check(JobStateReason.JOB_COMPLETED_WITH_WARNINGS.getValue() == 25, "JOB_COMPLETED_WITH_WARNINGS value == 25");
        check("job-completed-with-warnings".equals(JobStateReason.JOB_COMPLETED_WITH_WARNINGS.toString()), "JOB_COMPLETED_WITH_WARNINGS toString");
        check(JobStateReason.JOB_COMPLETED_WITH_ERRORS.getValue() == 26, "JOB_COMPLETED_WITH_ERRORS value == 26");
        check("job-completed-with-errors".equals(JobStateReason.JOB_COMPLETED_WITH_ERRORS.toString()), "JOB_COMPLETED_WITH_ERRORS toString");
        check(JobStateReason.JOB_RESTARTABLE.getValue() == 27, "JOB_RESTARTABLE value == 27");
        check("job-restartable".equals(JobStateReason.JOB_RESTARTABLE.toString()), "JOB_RESTARTABLE toString");
        check(JobStateReason.QUEUED_IN_DEVICE.getValue() == 28, "QUEUED_IN_DEVICE value == 28");
        check("queued-in-device".equals(JobStateReason.QUEUED_IN_DEVICE.toString()), "QUEUED_IN_DEVICE toString");
        check(JobStateReason.JOB_INCOMING.clone() == JobStateReason.JOB_INCOMING, "clone returns singleton");
        check(JobStateReason.JOB_INCOMING.hashCode() == 0, "hashCode == value");
        check(JobStateReason.JOB_INCOMING.equals(JobStateReason.JOB_INCOMING), "equals self");
        check(!JobStateReason.JOB_INCOMING.equals(JobStateReason.JOB_DATA_INSUFFICIENT), "distinct constants unequal");
        check(!JobStateReason.JOB_INCOMING.equals(null) && !JobStateReason.JOB_INCOMING.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
