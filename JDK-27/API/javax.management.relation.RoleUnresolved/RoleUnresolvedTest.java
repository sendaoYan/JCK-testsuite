/*
 * @test
 * @summary javax.management.relation.RoleUnresolved conformance: JMX API type verification.
 * @run main RoleUnresolvedTest
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

public class RoleUnresolvedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        List<ObjectName> vals = new ArrayList<ObjectName>();
        vals.add(new ObjectName("d:type=x"));
        RoleUnresolved ru = new RoleUnresolved("role", vals, RoleStatus.NO_ROLE_WITH_NAME);
        check(ru.getRoleName().equals("role"), "name");
        check(ru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME, "problem type");
        check(ru.getRoleValue().size() == 1, "value");
        ru.setProblemType(RoleStatus.ROLE_NOT_READABLE);
        check(ru.getProblemType() == RoleStatus.ROLE_NOT_READABLE, "set problem");
        check(ru.clone() instanceof RoleUnresolved, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
