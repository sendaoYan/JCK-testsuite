/*
 * @test
 * @summary javax.naming.directory.BasicAttributes conformance: white-box structure + black-box behavior.
 * @run main BasicAttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class BasicAttributesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.BasicAttributes");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface(), "not interface");
        check(Attributes.class.isAssignableFrom(c), "implements Attributes");
        check(hasMethod(c, "isCaseIgnored"), "isCaseIgnored");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "get"), "get");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "getIDs"), "getIDs");
        check(hasMethod(c, "put"), "put");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "clone"), "clone");
        check(hasMethod(c, "equals"), "equals");
        check(hasMethod(c, "hashCode"), "hashCode");
        check(hasMethod(c, "toString"), "toString");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == boolean.class), "ctor(boolean)");
    }

    static void testBlackBox() throws Exception {
        // case-sensitive default
        BasicAttributes as = new BasicAttributes();
        check(!as.isCaseIgnored(), "default case-sensitive");
        check(as.size() == 0, "empty size");

        as.put("cn", "Alice");
        check(as.size() == 1, "size 1");
        Attribute a = as.get("cn");
        check(a != null, "get cn");
        check("Alice".equals(a.get()), "get value");
        check(as.get("CN") == null, "case-sensitive get CN null");

        as.put("CN", "Bob");
        check(as.size() == 2, "size 2 distinct case");
        check("Bob".equals(as.get("CN").get()), "get CN");

        // put(Attribute)
        as.put(new BasicAttribute("sn", "Smith"));
        check(as.size() == 3, "size 3 after put(Attribute)");
        check("Smith".equals(as.get("sn").get()), "get sn");

        // getIDs / getAll
        List<String> ids = new ArrayList<>();
        NamingEnumeration<String> idEn = as.getIDs();
        while (idEn.hasMore()) ids.add(idEn.next());
        idEn.close();
        Collections.sort(ids);
        check(ids.equals(Arrays.asList("CN", "cn", "sn")), "getIDs sorted");

        List<String> all = new ArrayList<>();
        NamingEnumeration<? extends Attribute> allEn = as.getAll();
        while (allEn.hasMore()) all.add(allEn.next().getID());
        allEn.close();
        Collections.sort(all);
        check(all.equals(Arrays.asList("CN", "cn", "sn")), "getAll sorted ids");

        // remove
        check(as.remove("sn") != null, "remove sn");
        check(as.size() == 2, "size 2 after remove");
        check(as.remove("nope") == null, "remove absent null");

        // case-ignored attributes
        BasicAttributes ci = new BasicAttributes(true);
        check(ci.isCaseIgnored(), "case-ignored");
        ci.put("cn", "Alice");
        check(ci.get("CN") != null, "case-ignored get CN");
        check("Alice".equals(ci.get("CN").get()), "case-ignored value");
        check(ci.size() == 1, "case-ignored size 1");

        // equals / hashCode / clone
        BasicAttributes b1 = new BasicAttributes("cn", "v");
        BasicAttributes b2 = new BasicAttributes("cn", "v");
        check(b1.equals(b2), "equals same content");
        check(b1.hashCode() == b2.hashCode(), "hashCode same");
        BasicAttributes b3 = new BasicAttributes("cn", "different");
        check(!b1.equals(b3), "equals different value false");

        BasicAttributes cl = (BasicAttributes) b1.clone();
        check(cl != b1, "clone distinct");
        check(cl.equals(b1), "clone equals");
        check("v".equals(cl.get("cn").get()), "clone value");

        // toString
        check(as.toString().contains("cn"), "toString contains id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
