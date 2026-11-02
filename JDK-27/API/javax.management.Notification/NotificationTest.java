/*
 * @test
 * @summary javax.management.Notification conformance: JMX API type verification.
 * @run main NotificationTest
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

public class NotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Notification n = new Notification("type", "source", 1L, 100L, "msg");
        check(n.getType().equals("type"), "type");
        check(n.getSource().equals("source"), "source");
        check(n.getSequenceNumber() == 1L, "seq");
        check(n.getTimeStamp() == 100L, "timestamp");
        check(n.getMessage().equals("msg"), "message");
        n.setUserData("data");
        check(n.getUserData().equals("data"), "user data");
        n.setSequenceNumber(2L);
        check(n.getSequenceNumber() == 2L, "set seq");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
