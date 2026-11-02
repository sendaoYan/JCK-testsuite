/*
 * @test
 * @summary javax.management.relation.Relation conformance: JMX API type verification.
 * @run main RelationTest
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

public class RelationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Relation");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getRole"), "has getRole");
        check(has(mm, "getRoles"), "has getRoles");
        check(has(mm, "getRoleCardinality"), "has getRoleCardinality");
        check(has(mm, "getAllRoles"), "has getAllRoles");
        check(has(mm, "retrieveAllRoles"), "has retrieveAllRoles");
        check(has(mm, "setRole"), "has setRole");
        check(has(mm, "setRoles"), "has setRoles");
        check(has(mm, "getReferencedMBeans"), "has getReferencedMBeans");
        check(has(mm, "getRelationTypeName"), "has getRelationTypeName");
        check(has(mm, "getRelationServiceName"), "has getRelationServiceName");
        check(has(mm, "getRelationId"), "has getRelationId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
