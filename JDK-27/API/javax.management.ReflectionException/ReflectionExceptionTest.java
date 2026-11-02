/*
 * @test
 * @summary javax.management.ReflectionException conformance: JMX API type verification.
 * @run main ReflectionExceptionTest
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

public class ReflectionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Exception cause = new Exception("cause");
        ReflectionException e = new ReflectionException(cause, "msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof JMException && e instanceof Exception, "hierarchy");
        check(e.getTargetException() == cause, "target exception");
        check(e.getCause() == cause, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
