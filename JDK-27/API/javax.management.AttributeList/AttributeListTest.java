/*
 * @test
 * @summary javax.management.AttributeList conformance: JMX API type verification.
 * @run main AttributeListTest
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

public class AttributeListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        AttributeList l = new AttributeList();
        l.add(new Attribute("a", 1));
        l.add(new Attribute("b", 2));
        check(l.size() == 2, "size");
        check(l.asList().get(0).getName().equals("a"), "asList");
        check(l.get(0) instanceof Attribute, "get type");
        l.remove(0);
        check(l.size() == 1, "after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
