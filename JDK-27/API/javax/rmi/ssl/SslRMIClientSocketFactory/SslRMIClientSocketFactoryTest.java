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

    static void testWhiteBox() throws Exception {
        Class<?> c = SslRMIClientSocketFactory.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(RMIClientSocketFactory.class.isAssignableFrom(c), "implements RMIClientSocketFactory");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getMethod("createSocket", String.class, int.class) != null, "createSocket(String,int) declared");
    }

    static void testBlackBox() throws Exception {
        SslRMIClientSocketFactory f1 = new SslRMIClientSocketFactory();
        SslRMIClientSocketFactory f2 = new SslRMIClientSocketFactory();
        check(f1 instanceof RMIClientSocketFactory, "instanceof RMIClientSocketFactory");
        check(f1.equals(f1), "equals self");
        check(f1.equals(f2), "two default instances equal");
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
