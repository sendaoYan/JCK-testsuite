/*
 * @test
 * @summary javax.xml.soap.SOAPFault conformance: interface hierarchy and methods.
 * @run main SOAPFaultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPFaultTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPFault");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPBodyElement"), "implements javax.xml.soap.SOAPBodyElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setFaultCode"), "has method setFaultCode");
        check(names.contains("getFaultCodeAsName"), "has method getFaultCodeAsName");
        check(names.contains("getFaultCodeAsQName"), "has method getFaultCodeAsQName");
        check(names.contains("getFaultSubcodes"), "has method getFaultSubcodes");
        check(names.contains("removeAllFaultSubcodes"), "has method removeAllFaultSubcodes");
        check(names.contains("appendFaultSubcode"), "has method appendFaultSubcode");
        check(names.contains("getFaultCode"), "has method getFaultCode");
        check(names.contains("setFaultActor"), "has method setFaultActor");
        check(names.contains("getFaultActor"), "has method getFaultActor");
        check(names.contains("setFaultString"), "has method setFaultString");
        check(names.contains("getFaultString"), "has method getFaultString");
        check(names.contains("getFaultStringLocale"), "has method getFaultStringLocale");
        check(names.contains("hasDetail"), "has method hasDetail");
        check(names.contains("getDetail"), "has method getDetail");
        check(names.contains("addDetail"), "has method addDetail");
        check(names.contains("getFaultReasonLocales"), "has method getFaultReasonLocales");
        check(names.contains("getFaultReasonTexts"), "has method getFaultReasonTexts");
        check(names.contains("getFaultReasonText"), "has method getFaultReasonText");
        check(names.contains("addFaultReasonText"), "has method addFaultReasonText");
        check(names.contains("getFaultNode"), "has method getFaultNode");
        check(names.contains("setFaultNode"), "has method setFaultNode");
        check(names.contains("getFaultRole"), "has method getFaultRole");
        check(names.contains("setFaultRole"), "has method setFaultRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
