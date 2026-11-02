/*
 * @test
 * @summary javax.management.relation.RelationNotification conformance: JMX API type verification.
 * @run main RelationNotificationTest
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

public class RelationNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        RelationNotification n = new RelationNotification(
            RelationNotification.RELATION_BASIC_CREATION, new ObjectName("svc:type=s"), 1L, 100L,
            "msg", "relId", "relType", null, null);
        check(n.getRelationId().equals("relId"), "rel id");
        check(n.getRelationTypeName().equals("relType"), "type name");
        check(n.getObjectName() == null, "obj name null");
        check(n.getMBeansToUnregister().size() == 0, "unreg empty");
        check(n.getType().equals(RelationNotification.RELATION_BASIC_CREATION), "type");
        check(n.getMessage().equals("msg"), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
