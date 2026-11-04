/*
 * @test
 * @summary javax.naming.directory.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class AttributesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attributes");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(Cloneable.class.isAssignableFrom(c), "extends Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "isCaseIgnored"), "isCaseIgnored");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "get"), "get");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "getIDs"), "getIDs");
        check(hasMethod(c, "put"), "put(String,Object)");
        check(hasMethod(c, "put"), "put(Attribute)");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testBlackBox() throws Exception {
        // Exercise the contract through BasicAttributes.
        Attributes as = new BasicAttributes(false);
        check(!as.isCaseIgnored(), "isCaseIgnored");
        as.put("cn", "Alice");
        check(as.size() == 1, "size");
        Attribute a = as.get("cn");
        check(a != null, "get");
        check("Alice".equals(a.get()), "get value");
        as.put(new BasicAttribute("sn", "Smith"));
        check(as.size() == 2, "put(Attribute) size");
        List<String> ids = new ArrayList<>();
        NamingEnumeration<String> idEn = as.getIDs();
        while (idEn.hasMore()) ids.add(idEn.next());
        idEn.close();
        Collections.sort(ids);
        check(ids.equals(Arrays.asList("cn", "sn")), "getIDs");
        List<String> got = new ArrayList<>();
        NamingEnumeration<? extends Attribute> all = as.getAll();
        while (all.hasMore()) got.add(all.next().getID());
        all.close();
        Collections.sort(got);
        check(got.equals(Arrays.asList("cn", "sn")), "getAll");
        check(as.remove("sn") != null, "remove");
        Attributes cl = (Attributes) ((BasicAttributes) as).clone();
        check(cl.size() == 1, "clone size");
        check(cl.get("cn") != null, "clone get");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
