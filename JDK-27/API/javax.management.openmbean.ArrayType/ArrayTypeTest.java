/*
 * @test
 * @summary javax.management.openmbean.ArrayType conformance: JMX API type verification.
 * @run main ArrayTypeTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.security.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.security.auth.Subject;
import javax.management.*;
import javax.management.loading.*;
import javax.management.modelmbean.*;
import javax.management.monitor.*;
import javax.management.openmbean.*;
import javax.management.relation.*;
import javax.management.remote.*;
import javax.management.remote.rmi.*;
import javax.management.timer.*;

public class ArrayTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ArrayType<int[]> at = ArrayType.getPrimitiveArrayType(int[].class);
        check(at.getDimension() == 1, "dimension");
        check(at.getElementOpenType() == SimpleType.INTEGER, "element type");
        check(at.isPrimitiveArray(), "primitive array");
        check(at.isValue(new int[]{1, 2}), "isValue int");
        ArrayType<String[]> st = ArrayType.getArrayType(SimpleType.STRING);
        check(st.getElementOpenType() == SimpleType.STRING, "element string");
        check(!st.isPrimitiveArray(), "not primitive");
        check(st.isValue(new String[]{"a"}), "isValue string");
        check(new ArrayType<>(1, SimpleType.INTEGER).getDimension() == 1, "ctor dimension");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
