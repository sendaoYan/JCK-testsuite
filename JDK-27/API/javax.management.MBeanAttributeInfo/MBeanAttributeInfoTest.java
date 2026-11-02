/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: JMX API type verification.
 * @run main MBeanAttributeInfoTest
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

public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Name", "java.lang.String", "desc", true, true, false);
        check(ai.getName().equals("Name"), "name");
        check(ai.getType().equals("java.lang.String"), "type");
        check(ai.getDescription().equals("desc"), "description");
        check(ai.isReadable() && ai.isWritable() && !ai.isIs(), "read/write/is");
        MBeanAttributeInfo c = (MBeanAttributeInfo) ai.clone();
        check(c.equals(ai), "clone equals");
        check(ai.hashCode() == c.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
