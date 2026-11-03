/*
 * @test
 * @summary java.beans.PersistenceDelegate conformance: white-box structure + black-box behavior.
 * @run main PersistenceDelegateTest
 */

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

public class PersistenceDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class DummyPD extends PersistenceDelegate {
        boolean wrote;
        protected Expression instantiate(Object oldInstance, Encoder out) { return null; }
        public void writeObject(Object oldInstance, Encoder out) { wrote = true; }
        boolean mutatesToPub(Object o, Object n) { return mutatesTo(o, n); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PersistenceDelegate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        java.lang.reflect.Method im = c.getDeclaredMethod("instantiate", Object.class, Encoder.class);
        check(java.lang.reflect.Modifier.isAbstract(im.getModifiers()), "instantiate abstract");
        check(java.lang.reflect.Modifier.isProtected(im.getModifiers()), "instantiate protected");
    }

    static void testBlackBox() throws Exception {
        DummyPD pd = new DummyPD();
        Encoder enc = new Encoder();
        pd.writeObject("x", enc);
        check(pd.wrote, "writeObject invoked");
        check(pd.mutatesToPub("a", "a"), "mutatesTo");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
