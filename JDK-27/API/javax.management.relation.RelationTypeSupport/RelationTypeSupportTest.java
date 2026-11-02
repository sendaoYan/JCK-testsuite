/*
 * @test
 * @summary javax.management.relation.RelationTypeSupport conformance: JMX API type verification.
 * @run main RelationTypeSupportTest
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

public class RelationTypeSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        RoleInfo ri = new RoleInfo("role", "java.lang.String", true, true, 1, 1, "java.lang.String");
        RelationTypeSupport rt = new RelationTypeSupport("myType", new RoleInfo[]{ri});
        check(rt.getRelationTypeName().equals("myType"), "type name");
        check(rt.getRoleInfos().size() == 1, "role infos");
        check(rt.getRoleInfo("role").getName().equals("role"), "role info");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
