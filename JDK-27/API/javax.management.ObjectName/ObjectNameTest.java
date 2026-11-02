/*
 * @test
 * @summary javax.management.ObjectName conformance: JMX API type verification.
 * @run main ObjectNameTest
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

public class ObjectNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ObjectName on = new ObjectName("domain:key=value");
        check(on.getDomain().equals("domain"), "domain");
        check(on.getKeyProperty("key").equals("value"), "key prop");
        check(on.getKeyPropertyList().size() == 1, "prop list size");
        check(!on.isPattern(), "not pattern");
        check(!on.isPropertyPattern(), "not prop pattern");
        check(on.apply(on), "apply self");
        check(on.equals(new ObjectName("domain:key=value")), "equals");
        check(on.hashCode() == new ObjectName("domain:key=value").hashCode(), "hashCode");
        check(on.compareTo(new ObjectName("domain:key=value")) == 0, "compareTo");
        check(ObjectName.getInstance("domain:key=value").equals(on), "getInstance");
        ObjectName p = new ObjectName("domain:key=*");
        check(p.isPattern(), "pattern");
        check(p.isPropertyValuePattern(), "prop value pattern");
        check(p.apply(on), "pattern applies");
        check(ObjectName.unquote(ObjectName.quote("x")).equals("x"), "quote/unquote");
        check(ObjectName.WILDCARD.isPattern(), "wildcard");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
