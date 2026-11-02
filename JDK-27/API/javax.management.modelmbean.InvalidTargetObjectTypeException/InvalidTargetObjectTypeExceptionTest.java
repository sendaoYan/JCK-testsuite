/*
 * @test
 * @summary javax.management.modelmbean.InvalidTargetObjectTypeException conformance: JMX API type verification.
 * @run main InvalidTargetObjectTypeExceptionTest
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

public class InvalidTargetObjectTypeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        InvalidTargetObjectTypeException e = new InvalidTargetObjectTypeException("msg");
        check(e.getMessage() != null, "message non-null");
        check(e instanceof Exception, "hierarchy");
        InvalidTargetObjectTypeException e2 = new InvalidTargetObjectTypeException();
        check(e2.getMessage() != null, "no-arg message non-null");
        InvalidTargetObjectTypeException e3 = new InvalidTargetObjectTypeException(new Exception("cause"), "msg2");
        check(e3.getMessage() != null, "target message non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
