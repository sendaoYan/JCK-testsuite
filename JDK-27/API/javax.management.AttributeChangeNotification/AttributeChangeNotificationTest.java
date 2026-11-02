/*
 * @test
 * @summary javax.management.AttributeChangeNotification conformance: JMX API type verification.
 * @run main AttributeChangeNotificationTest
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

public class AttributeChangeNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Object old = "old";
        Object nev = "new";
        AttributeChangeNotification n = new AttributeChangeNotification("src", 1L, 100L, "msg", "AttrName", "java.lang.String", old, nev);
        check(n.getAttributeName().equals("AttrName"), "attr name");
        check(n.getAttributeType().equals("java.lang.String"), "attr type");
        check(n.getOldValue() == old, "old value");
        check(n.getNewValue() == nev, "new value");
        check(n.getType().equals(AttributeChangeNotification.ATTRIBUTE_CHANGE), "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
