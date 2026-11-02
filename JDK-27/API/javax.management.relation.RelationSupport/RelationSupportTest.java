/*
 * @test
 * @summary javax.management.relation.RelationSupport conformance: JMX API type verification.
 * @run main RelationSupportTest
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

public class RelationSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        List<ObjectName> vals = new ArrayList<ObjectName>();
        vals.add(new ObjectName("d:type=x"));
        Role r = new Role("role", vals);
        RoleList rl = new RoleList();
        rl.add(r);
        RelationSupport sup = new RelationSupport("relId", new ObjectName("svc:type=s"), "myType", rl);
        check(sup.getRelationId().equals("relId"), "rel id");
        check(sup.getRelationTypeName().equals("myType"), "type name");
        check(sup.isInRelationService().equals(Boolean.FALSE), "not in service");
        check(sup.getRelationServiceName().equals(new ObjectName("svc:type=s")), "service name");
        check(sup.retrieveAllRoles().size() == 1, "all roles");
        check(sup.getRoleCardinality("role").intValue() == 1, "cardinality");
        Map<ObjectName, List<String>> refs = sup.getReferencedMBeans();
        check(refs.containsKey(new ObjectName("d:type=x")), "referenced mbean");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
