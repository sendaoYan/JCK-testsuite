/*
 * @test
 * @summary javax.management.openmbean.CompositeDataInvocationHandler conformance: JMX API type verification.
 * @run main CompositeDataInvocationHandlerTest
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

public class CompositeDataInvocationHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String[] names = {"name", "value"};
        String[] descs = {"name desc", "value desc"};
        OpenType<?>[] types = {SimpleType.STRING, SimpleType.INTEGER};
        CompositeType ct = new CompositeType("MyType", "desc", names, descs, types);
        CompositeDataSupport cd = new CompositeDataSupport(ct, names, new Object[]{"n", 5});
        CompositeDataInvocationHandler h = new CompositeDataInvocationHandler(cd);
        check(h.getCompositeData().equals(cd), "getCompositeData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
