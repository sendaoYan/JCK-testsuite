/*
 * @test
 * @summary javax.naming.directory.ModificationItem conformance: white-box structure + black-box behavior.
 * @run main ModificationItemTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.directory.*;

public class ModificationItemTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.ModificationItem");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == int.class && x.getParameterTypes()[1] == Attribute.class), "ctor(int,Attribute)");
        for (String m : new String[]{"getModificationOp", "getAttribute", "toString"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        Attribute a = new BasicAttribute("cn", "Alice");
        ModificationItem add = new ModificationItem(DirContext.ADD_ATTRIBUTE, a);
        check(add.getModificationOp() == DirContext.ADD_ATTRIBUTE, "ADD op");
        check(add.getAttribute() == a, "attribute identity");

        ModificationItem replace = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, a);
        check(replace.getModificationOp() == DirContext.REPLACE_ATTRIBUTE, "REPLACE op");

        ModificationItem remove = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a);
        check(remove.getModificationOp() == DirContext.REMOVE_ATTRIBUTE, "REMOVE op");

        check(add.getAttribute().getID().equals("cn"), "attr id via getAttribute");
        check("Alice".equals(add.getAttribute().get()), "attr value via getAttribute");

        // toString identifies the operation
        check(add.toString().toLowerCase(Locale.ROOT).contains("add"), "toString ADD");
        check(replace.toString().toLowerCase(Locale.ROOT).contains("replace"), "toString REPLACE");
        check(remove.toString().toLowerCase(Locale.ROOT).contains("remove"), "toString REMOVE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
