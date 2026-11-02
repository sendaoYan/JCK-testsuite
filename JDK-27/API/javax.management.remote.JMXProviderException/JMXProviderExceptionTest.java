/*
 * @test
 * @summary javax.management.remote.JMXProviderException conformance: JMX API type verification.
 * @run main JMXProviderExceptionTest
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

public class JMXProviderExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        JMXProviderException e = new JMXProviderException("msg", new Exception("cause"));
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof java.io.IOException, "hierarchy");
        check(e.getCause() instanceof Exception, "cause");
        JMXProviderException e2 = new JMXProviderException();
        check(e2.getMessage() == null, "null message");
        JMXProviderException e3 = new JMXProviderException("only");
        check(e3.getMessage().equals("only"), "string ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
