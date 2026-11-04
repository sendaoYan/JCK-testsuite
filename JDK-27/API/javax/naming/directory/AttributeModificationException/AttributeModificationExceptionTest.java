/*
 * @test
 * @summary javax.naming.directory.AttributeModificationException conformance: white-box structure + black-box behavior.
 * @run main AttributeModificationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class AttributeModificationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.AttributeModificationException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        for (String m : new String[]{"setUnexecutedModifications", "getUnexecutedModifications", "toString"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() {
        AttributeModificationException e = new AttributeModificationException("failed");
        check("failed".equals(e.getMessage()), "getMessage");
        check(e.getUnexecutedModifications() == null, "unexecuted default null");

        ModificationItem[] items = {
            new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("cn", "x")),
            new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("sn", "y"))
        };
        e.setUnexecutedModifications(items);
        ModificationItem[] got = e.getUnexecutedModifications();
        check(got != null, "unexecuted not null");
        check(got.length == 2, "unexecuted length");
        check(got[0].getModificationOp() == DirContext.ADD_ATTRIBUTE, "item0 op");
        check(got[1].getModificationOp() == DirContext.REMOVE_ATTRIBUTE, "item1 op");
        check("cn".equals(got[0].getAttribute().getID()), "item0 attr id");

        AttributeModificationException e2 = new AttributeModificationException();
        check(e2.getMessage() == null, "null message");
        check(e2 instanceof NamingException, "instanceof NamingException");
        check(e.toString().contains("failed"), "toString contains message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
