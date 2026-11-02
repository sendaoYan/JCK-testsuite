/*
 * @test
 * @summary javax.management.AttributeChangeNotificationFilter conformance: JMX API type verification.
 * @run main AttributeChangeNotificationFilterTest
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

public class AttributeChangeNotificationFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Attr");
        check(f.getEnabledAttributes().size() == 1, "enabled size");
        AttributeChangeNotification n = new AttributeChangeNotification("s", 1L, 1L, "m", "Attr", "int", "o", "n");
        check(f.isNotificationEnabled(n), "enabled");
        check(!f.isNotificationEnabled(new AttributeChangeNotification("s", 1L, 1L, "m", "Other", "int", "o", "n")), "not enabled");
        f.disableAttribute("Attr");
        check(f.getEnabledAttributes().size() == 0, "disabled");
        f.disableAllAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
