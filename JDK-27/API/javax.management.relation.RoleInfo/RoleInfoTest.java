/*
 * @test
 * @summary javax.management.relation.RoleInfo conformance: JMX API type verification.
 * @run main RoleInfoTest
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

public class RoleInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        RoleInfo ri = new RoleInfo("role", "java.lang.String", true, true);
        check(ri.getName().equals("role"), "name");
        check(ri.isReadable() && ri.isWritable(), "readable/writable");
        check(ri.getRefMBeanClassName().equals("java.lang.String"), "ref class");
        check(ri.getMinDegree() == 1 && ri.getMaxDegree() == 1, "degrees");
        check(ri.checkMinDegree(1) && ri.checkMaxDegree(1), "check degrees");
        check(RoleInfo.ROLE_CARDINALITY_INFINITY == -1, "infinity");
        RoleInfo ri2 = new RoleInfo(ri);
        check(ri2.getName().equals("role"), "copy name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
