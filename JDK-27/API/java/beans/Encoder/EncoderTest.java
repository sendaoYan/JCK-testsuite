/*
 * @test
 * @summary java.beans.Encoder conformance: white-box structure + black-box behavior.
 * @run main EncoderTest
 */

import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class PD extends PersistenceDelegate {
        protected Expression instantiate(Object oldInstance, Encoder out) { return null; }
        public void writeObject(Object oldInstance, Encoder out) {}
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Encoder");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        Encoder enc = new Encoder();
        ExceptionListener l = new ExceptionListener() { public void exceptionThrown(Exception e) {} };
        enc.setExceptionListener(l);
        check(l == enc.getExceptionListener(), "exceptionListener set/get");
        check(enc.getPersistenceDelegate(String.class) != null, "default persistenceDelegate");
        PD pd = new PD();
        enc.setPersistenceDelegate(String.class, pd);
        check(pd == enc.getPersistenceDelegate(String.class), "setPersistenceDelegate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
