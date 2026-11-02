/*
 * @test
 * @summary javax.management.relation.RelationService conformance: JMX API type verification.
 * @run main RelationServiceTest
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

public class RelationServiceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=RelationServiceTest");
        RelationService rs = new RelationService(true);
        mbs.registerMBean(rs, on);
        rs.isActive();
        check(rs.getPurgeFlag(), "purge flag true");
        rs.setPurgeFlag(false);
        check(!rs.getPurgeFlag(), "purge flag false");
        rs.setPurgeFlag(true);
        RoleInfo ri = new RoleInfo("role", "java.lang.String", true, true, 1, 1, "java.lang.String");
        rs.createRelationType("myType", new RoleInfo[]{ri});
        check(rs.getAllRelationTypeNames().contains("myType"), "type created");
        check(rs.getRoleInfos("myType").size() == 1, "role infos");
        check(rs.findRelationsOfType("myType").isEmpty(), "no relations");
        check(rs.getAllRelationIds().isEmpty(), "no rel ids");
        check(rs.hasRelation("nope").equals(Boolean.FALSE), "hasRelation false");
        rs.removeRelationType("myType");
        check(!rs.getAllRelationTypeNames().contains("myType"), "type removed");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
