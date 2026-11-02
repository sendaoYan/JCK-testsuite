/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfoSupport conformance: JMX API type verification.
 * @run main OpenMBeanInfoSupportTest
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

public class OpenMBeanInfoSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        OpenMBeanAttributeInfo ai = new OpenMBeanAttributeInfoSupport("Name", "desc", SimpleType.STRING, true, true, false);
        OpenMBeanConstructorInfo ci = new OpenMBeanConstructorInfoSupport("Test", "desc", new OpenMBeanParameterInfo[0]);
        OpenMBeanOperationInfo oi = new OpenMBeanOperationInfoSupport("op", "desc", new OpenMBeanParameterInfo[0], SimpleType.INTEGER, MBeanOperationInfo.INFO);
        OpenMBeanInfoSupport info = new OpenMBeanInfoSupport("Test", "desc",
            new OpenMBeanAttributeInfo[]{ai}, new OpenMBeanConstructorInfo[]{ci},
            new OpenMBeanOperationInfo[]{oi}, new MBeanNotificationInfo[0]);
        check(info.getClassName().equals("Test"), "className");
        check(info.getAttributes().length == 1, "attributes");
        check(info.getConstructors().length == 1, "constructors");
        check(info.getOperations().length == 1, "operations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
