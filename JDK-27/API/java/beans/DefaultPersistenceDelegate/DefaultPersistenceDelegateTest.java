/*
 * @test
 * @summary java.beans.DefaultPersistenceDelegate conformance: white-box structure + black-box behavior.
 * @run main DefaultPersistenceDelegateTest
 */

import java.beans.DefaultPersistenceDelegate;
import java.beans.PersistenceDelegate;

public class DefaultPersistenceDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.DefaultPersistenceDelegate");
        check(PersistenceDelegate.class.isAssignableFrom(c), "extends PersistenceDelegate");
    }

    static void testBlackBox() throws Exception {
        DefaultPersistenceDelegate dpd = new DefaultPersistenceDelegate();
        check(dpd instanceof PersistenceDelegate, "instanceof PersistenceDelegate");
        DefaultPersistenceDelegate dpd2 = new DefaultPersistenceDelegate(new String[]{"x", "y"});
        check(dpd2 instanceof PersistenceDelegate, "array ctor instanceof");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
