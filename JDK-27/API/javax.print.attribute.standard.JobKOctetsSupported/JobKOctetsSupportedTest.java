/*
 * @test
 * @summary javax.print.attribute.standard.JobKOctetsSupported conformance: white-box structure + black-box behavior.
 * @run main JobKOctetsSupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobKOctetsSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobKOctetsSupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(SetOfIntegerSyntax.class.isAssignableFrom(c), "extends SetOfIntegerSyntax");
        check(SupportedValuesAttribute.class.isAssignableFrom(c), "implements SupportedValuesAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobKOctetsSupported p = new JobKOctetsSupported(1, 3);
        check("job-k-octets-supported".equals(p.getName()), "getName");
        check(p.getCategory() == JobKOctetsSupported.class, "getCategory");
        int[][] m = p.getMembers();
        check(m.length == 1 && m[0][0] == 1 && m[0][1] == 3, "getMembers");
        check("1-3".equals(p.toString()), "toString");
        check(p.contains(1) && p.contains(2) && p.contains(3), "contains in range");
        check(!p.contains(0) && !p.contains(4), "contains out of range");
        check(p.contains(new Copies(2)), "contains(IntegerSyntax)");
        check(p.next(-1) == 1 && p.next(1) == 2 && p.next(3) == -1, "next");
        check(p.equals(new JobKOctetsSupported(1, 3)), "equals same");
        check(!p.equals(new JobKOctetsSupported(1, 4)), "different unequal");
        check(!p.equals(null) && !p.equals("x"), "not equals null/foreign");
        check(p.hashCode() == new JobKOctetsSupported(1, 3).hashCode(), "hashCode consistent");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
