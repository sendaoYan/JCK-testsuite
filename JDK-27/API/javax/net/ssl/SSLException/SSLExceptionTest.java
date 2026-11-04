/*
 * @test
 * @summary javax.net.ssl.SSLException conformance: constructor chain + hierarchy.
 * @run main SSLExceptionTest
 */
import java.io.*;
import java.lang.reflect.*;
import javax.net.ssl.*;
public class SSLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 3, "3 public constructors (String), (String,Throwable), (Throwable)");

        SSLException e1 = new SSLException("reason");
        check("reason".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");
        IllegalArgumentException cause = new IllegalArgumentException("root");
        SSLException e2 = new SSLException("outer", cause);
        check("outer".equals(e2.getMessage()) && e2.getCause() == cause,
                "(String,Throwable) ctor keeps message and exact cause object");
        SSLException e3 = new SSLException(cause);
        check(cause == e3.getCause(), "(Throwable) ctor keeps exact cause");
        check(cause.toString().equals(e3.getMessage()), "(Throwable) ctor message is cause.toString()");
        check(e3 instanceof IOException, "SSLException is an IOException");
        check(e2 instanceof java.io.Serializable, "Serializable through IOException");

        // serialization round trip keeps the message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(e2); }
        SSLException back = (SSLException) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check("outer".equals(back.getMessage()), "serialization keeps message");
        check(back.getCause() instanceof IllegalArgumentException && "root".equals(back.getCause().getMessage()),
                "serialization keeps cause type and message");
        check(new SSLException((String) null).getMessage() == null, "null message preserved as null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
