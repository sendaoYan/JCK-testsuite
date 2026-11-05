/*
 * @test
 * @summary javax.rmi.ssl.SslRMIClientSocketFactory conformance: white-box structure + black-box behavior.
 * @run main SslRMIClientSocketFactoryTest
 */

import java.io.*;
import java.lang.reflect.Modifier;
import java.rmi.server.RMIClientSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class SslRMIClientSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.ssl.SslRMIClientSocketFactory");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(!c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Class.forName("java.rmi.server.RMIClientSocketFactory").isAssignableFrom(c), "implements/extends java.rmi.server.RMIClientSocketFactory");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "createSocket"), "declares createSocket");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
    }

    static void testBlackBox() throws Exception {
        SslRMIClientSocketFactory f1 = new SslRMIClientSocketFactory();
        SslRMIClientSocketFactory f2 = new SslRMIClientSocketFactory();
        check(f1 instanceof RMIClientSocketFactory, "instanceof RMIClientSocketFactory");
        check(f1 instanceof Serializable, "instanceof Serializable");
        check(f1.equals(f1), "equals self");
        check(f1.equals(f2), "two default instances equal (stateless value semantics)");
        check(f1.hashCode() == f2.hashCode(), "equal instances same hashCode");
        check(!f1.equals(null), "not equals null");
        check(!f1.equals(new Object()), "not equals other type");

        // Java serialization round trip (factory has no instance state)
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(f1); oout.flush();
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        SslRMIClientSocketFactory f3 = (SslRMIClientSocketFactory) oin.readObject();
        check(f1.equals(f3), "serialization round trip equals");
        check(f3.hashCode() == f1.hashCode(), "serialization round trip hashCode");

        // createSocket to a dead local port fails with IOException (no network needed:
        // connect to an unbound ephemeral port on loopback)
        try {
            f1.createSocket("127.0.0.1", 1);
            check(false, "expected IOException connecting to closed port");
        } catch (java.io.IOException expected) {
            check(true, "createSocket to closed port -> IOException");
        }

        // null host -> NPE/IAE from the underlying socket layer (rejected, not accepted)
        try {
            f1.createSocket(null, 0);
            check(false, "expected exception for null host");
        } catch (Exception expected) {
            check(true, "createSocket(null, 0) rejected");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
