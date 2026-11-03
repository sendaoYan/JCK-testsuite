/*
 * @test
 * @summary javax.naming.Referenceable conformance: white-box structure + black-box behavior.
 * @run main ReferenceableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

public class ReferenceableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class BindableObject implements Referenceable {
        int produced;
        public Reference getReference() {
            produced++;
            return new Reference(getClass().getName(), new StringRefAddr("cfg", "value"));
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Referenceable.class;
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        Method m = c.getMethod("getReference");
        check(m.getReturnType() == Reference.class, "getReference returns Reference");
        check(java.util.Arrays.asList(m.getExceptionTypes()).contains(javax.naming.NamingException.class),
                "declares NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BindableObject bo = new BindableObject();
        Reference r = bo.getReference();
        check(r != null, "reference produced");
        check(r.getClassName().equals(bo.getClass().getName()), "className points to class");
        check(r.get("cfg").getContent().equals("value"), "addr content");
        check(bo.produced == 1, "invoked once");
        check(bo.getReference().equals(bo.getReference()), "equal references each call");
        check(bo.produced == 3, "each call produces a reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
