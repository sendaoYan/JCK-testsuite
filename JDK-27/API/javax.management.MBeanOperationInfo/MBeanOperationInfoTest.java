/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: JMX API type verification.
 * @run main MBeanOperationInfoTest
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

public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "java.lang.String", MBeanOperationInfo.ACTION);
        check(oi.getName().equals("op"), "name");
        check(oi.getReturnType().equals("java.lang.String"), "return type");
        check(oi.getImpact() == MBeanOperationInfo.ACTION, "impact");
        check(oi.getSignature().length == 0, "signature");
        check(MBeanOperationInfo.INFO != MBeanOperationInfo.ACTION && MBeanOperationInfo.ACTION_INFO != MBeanOperationInfo.UNKNOWN, "constants");
        MBeanOperationInfo c = (MBeanOperationInfo) oi.clone();
        check(c.equals(oi), "clone equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
