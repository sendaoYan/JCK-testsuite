/*
 * @test
 * @summary javax.management.openmbean.SimpleType conformance: JMX API type verification.
 * @run main SimpleTypeTest
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

public class SimpleTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(SimpleType.INTEGER.isValue(5), "INTEGER isValue");
        check(!SimpleType.INTEGER.isValue("x"), "INTEGER not string");
        check(SimpleType.STRING.isValue("hello"), "STRING isValue");
        check(SimpleType.BOOLEAN.isValue(true), "BOOLEAN isValue");
        check(SimpleType.INTEGER.getClassName().equals("java.lang.Integer"), "className");
        check(SimpleType.STRING.getTypeName().equals("java.lang.String"), "typeName");
        check(!SimpleType.INTEGER.isArray(), "not array");
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER), "equals");
        check(SimpleType.INTEGER.hashCode() == SimpleType.INTEGER.hashCode(), "hashCode");
        check(SimpleType.VOID != null && SimpleType.BIGDECIMAL != null && SimpleType.BIGINTEGER != null && SimpleType.DATE != null && SimpleType.OBJECTNAME != null, "constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
