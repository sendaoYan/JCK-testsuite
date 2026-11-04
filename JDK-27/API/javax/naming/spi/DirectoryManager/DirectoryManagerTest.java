/*
 * @test
 * @summary javax.naming.spi.DirectoryManager conformance: white-box structure + black-box behavior.
 * @run main DirectoryManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.CannotProceedException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.DirStateFactory;
import javax.naming.spi.DirectoryManager;

public class DirectoryManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirectoryManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass().getName().equals("javax.naming.spi.NamingManager"),
                "extends NamingManager");
        check(hasMethod(c, "getContinuationDirContext"), "getContinuationDirContext");
        check(hasMethod(c, "getObjectInstance"), "getObjectInstance");
        check(hasMethod(c, "getStateToBind"), "getStateToBind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // getObjectInstance: with no object factory installed, an ordinary object is returned unchanged.
        String plain = "plain-object";
        Attributes attrs = new BasicAttributes("cn", "v");
        Object r = DirectoryManager.getObjectInstance(plain, null, null, null, attrs);
        check(r == plain, "getObjectInstance returns plain object unchanged");

        Object rNull = DirectoryManager.getObjectInstance(null, null, null, null, attrs);
        check(rNull == null, "getObjectInstance(null) returns null");

        // getStateToBind: with no state factory, the object and attributes are wrapped unchanged.
        DirStateFactory.Result res = DirectoryManager.getStateToBind(plain, null, null, null, attrs);
        check(res != null, "getStateToBind returns non-null Result");
        check(res.getObject() == plain, "getStateToBind keeps object");
        check(res.getAttributes() == attrs, "getStateToBind keeps attributes");

        // getContinuationDirContext builds a usable DirContext carrying the continuation.
        CannotProceedException cpe = new CannotProceedException();
        cpe.setEnvironment(new Hashtable<>());
        DirContext dctx = DirectoryManager.getContinuationDirContext(cpe);
        check(dctx != null, "getContinuationDirContext returns non-null");
        check(dctx instanceof DirContext, "result is a DirContext");
        dctx.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
