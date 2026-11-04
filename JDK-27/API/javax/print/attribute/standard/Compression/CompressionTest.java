/*
 * @test
 * @summary javax.print.attribute.standard.Compression conformance: white-box structure + black-box behavior.
 * @run main CompressionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class CompressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Compression");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Compression.NONE.getCategory() == Compression.class, "getCategory");
        check("compression".equals(Compression.NONE.getName()), "getName");
        check(Compression.NONE.getValue() == 0, "NONE value == 0");
        check("none".equals(Compression.NONE.toString()), "NONE toString");
        check(Compression.DEFLATE.getValue() == 1, "DEFLATE value == 1");
        check("deflate".equals(Compression.DEFLATE.toString()), "DEFLATE toString");
        check(Compression.GZIP.getValue() == 2, "GZIP value == 2");
        check("gzip".equals(Compression.GZIP.toString()), "GZIP toString");
        check(Compression.COMPRESS.getValue() == 3, "COMPRESS value == 3");
        check("compress".equals(Compression.COMPRESS.toString()), "COMPRESS toString");
        check(Compression.NONE.clone() == Compression.NONE, "clone returns singleton");
        check(Compression.NONE.hashCode() == 0, "hashCode == value");
        check(Compression.NONE.equals(Compression.NONE), "equals self");
        check(!Compression.NONE.equals(Compression.DEFLATE), "distinct constants unequal");
        check(!Compression.NONE.equals(null) && !Compression.NONE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
