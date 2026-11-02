/*
 * @test
 * @summary javax.management.modelmbean.DescriptorSupport conformance: JMX API type verification.
 * @run main DescriptorSupportTest
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

public class DescriptorSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        DescriptorSupport d = new DescriptorSupport();
        d.setField("name", "value");
        check(d.getFieldValue("name").equals("value"), "field value");
        check(d.getFieldNames().length == 1, "field names");
        d.setFields(new String[]{"a", "b"}, new Object[]{1, 2});
        check(d.getFieldValue("a").equals(1), "field a");
        DescriptorSupport c = (DescriptorSupport) d.clone();
        check(c.equals(d), "clone equals");
        d.removeField("a");
        check(d.getFieldNames().length == 2, "remove field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
