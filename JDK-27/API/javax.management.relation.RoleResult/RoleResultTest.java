/*
 * @test
 * @summary javax.management.relation.RoleResult conformance: JMX API type verification.
 * @run main RoleResultTest
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

public class RoleResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        RoleList roles = new RoleList();
        roles.add(new Role("a", new ArrayList<ObjectName>()));
        RoleUnresolvedList unresolved = new RoleUnresolvedList();
        RoleResult rr = new RoleResult(roles, unresolved);
        check(rr.getRoles().size() == 1, "roles");
        check(rr.getRolesUnresolved().size() == 0, "unresolved");
        rr.setRoles(new RoleList());
        check(rr.getRoles().size() == 0, "set roles");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
