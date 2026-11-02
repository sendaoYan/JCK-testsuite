/*
 * @test
 * @summary javax.management.relation.RoleList conformance: JMX API type verification.
 * @run main RoleListTest
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

public class RoleListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        RoleList l = new RoleList();
        l.add(new Role("a", new ArrayList<ObjectName>()));
        l.add(new Role("b", new ArrayList<ObjectName>()));
        check(l.size() == 2, "size");
        check(l.asList().get(0).getRoleName().equals("a"), "asList");
        l.remove(0);
        check(l.size() == 1, "after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
