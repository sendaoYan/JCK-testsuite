/*
 * @test
 * @summary java.net.HttpRetryException conformance: white-box structure + black-box
 *          behavior. Deep contract coverage: both constructors, responseCode()/getReason()/
 *          getLocation() getter matrix (incl. null reason and arbitrary codes), message
 *          == reason, exact serialVersionUID, serialization round-trip preserving all
 *          three fields, IOException hierarchy.
 * @run main HttpRetryExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class HttpRetryExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ------------------------------------------------------------------
    // original checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginal() {
        java.net.HttpRetryException e = new java.net.HttpRetryException("retry", 503);
        check(e instanceof java.io.IOException, "is IOException");
        check("retry".equals(e.getMessage()), "getMessage");
        check(e.responseCode() == 503, "responseCode");
        check(e.getLocation() == null, "location null");
        java.net.HttpRetryException e2 = new java.net.HttpRetryException("retry", 503, "http://x/");
        check("http://x/".equals(e2.getLocation()), "getLocation");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpRetryException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getSuperclass() == java.io.IOException.class, "direct superclass IOException");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getConstructor(String.class, int.class, String.class) != null, "ctor(String,int,String)");
        check(c.getMethod("responseCode") != null, "responseCode");
        check(c.getMethod("responseCode").getReturnType() == int.class, "responseCode returns int");
        check(c.getMethod("getReason") != null, "getReason");
        check(c.getMethod("getLocation") != null, "getLocation");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }

    static void testBlackBox() throws Exception {
        HttpRetryException e = new HttpRetryException("moved", 302);
        check(e.responseCode() == 302, "responseCode");
        check(e.getReason().equals("moved"), "reason");
        check(e.getLocation() == null, "location null default");
        check(e instanceof java.io.IOException, "instanceof IOException");
        check("moved".equals(e.getMessage()), "getMessage == reason");
        check(e.toString().contains("302") || e.toString().contains("moved"), "toString informative");

        HttpRetryException e2 = new HttpRetryException("redirect", 301, "http://example.com/");
        check(e2.responseCode() == 301, "responseCode with location");
        check(e2.getReason().equals("redirect"), "reason with location");
        check(e2.getLocation().equals("http://example.com/"), "location");
        check("redirect".equals(e2.getMessage()), "getMessage == reason (3-arg)");

        // null reason / null location are permitted and observable
        HttpRetryException e3 = new HttpRetryException(null, 503);
        check(e3.getReason() == null, "null reason");
        check(e3.getMessage() == null, "null message follows reason");
        check(e3.responseCode() == 503, "code with null reason");
        HttpRetryException e4 = new HttpRetryException("r", 300, null);
        check(e4.getLocation() == null, "null location");
        // arbitrary codes are accepted (no range validation)
        check(new HttpRetryException("a", 0).responseCode() == 0, "code 0 accepted");
        check(new HttpRetryException("b", -1).responseCode() == -1, "negative code accepted");
        check(new HttpRetryException("c", Integer.MAX_VALUE).responseCode() == Integer.MAX_VALUE,
                "MAX_VALUE code accepted");
        // empty strings
        HttpRetryException e5 = new HttpRetryException("", 100, "");
        check("".equals(e5.getReason()) && "".equals(e5.getLocation()), "empty reason/location");
        // cause state
        check(e.getCause() == null, "cause default null");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
    }

    static void testSerialization() throws Exception {
        // exact serialVersionUID (identical on JDK-8 and JDK-27, probed)
        long uid = ObjectStreamClass.lookup(HttpRetryException.class).getSerialVersionUID();
        check(uid == -9186022286469111381L, "serialVersionUID exact, got " + uid);
        HttpRetryException e = roundTrip(new HttpRetryException("why", 302, "http://loc/"));
        check(e instanceof HttpRetryException, "round-trip class");
        check(e.responseCode() == 302, "round-trip responseCode");
        check("why".equals(e.getReason()), "round-trip reason");
        check("http://loc/".equals(e.getLocation()), "round-trip location");
        check("why".equals(e.getMessage()), "round-trip message");
        HttpRetryException e2 = roundTrip(new HttpRetryException(null, 503));
        check(e2.getReason() == null && e2.responseCode() == 503, "round-trip null reason");
        check(e2.getLocation() == null, "round-trip null location");
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
