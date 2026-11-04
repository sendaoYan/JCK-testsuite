/*
 * @test
 * @summary javax.naming.directory.SearchResult conformance: white-box structure + black-box behavior.
 * @run main SearchResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class SearchResultTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.SearchResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Binding.class.isAssignableFrom(c), "extends Binding");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 3), "ctor(String,Object,Attributes)");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 4), "ctor(String,Object,Attributes,boolean)");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 5), "ctor(String,String,Object,Attributes,boolean)");
        for (String m : new String[]{"getAttributes", "setAttributes", "toString"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        BasicAttributes attrs = new BasicAttributes("cn", "Alice");
        SearchResult sr = new SearchResult("cn=Alice", "someObject", attrs);
        check("cn=Alice".equals(sr.getName()), "name");
        check("someObject".equals(sr.getObject()), "object");
        check(sr.getAttributes() == attrs, "attributes identity");
        check(sr.isRelative(), "default relative true");
        check("java.lang.String".equals(sr.getClassName()), "default className from object");

        // 5-arg constructor sets className + relative
        SearchResult sr2 = new SearchResult("n", "com.example.Type", "obj", attrs, true);
        check("n".equals(sr2.getName()), "5-arg name");
        check("com.example.Type".equals(sr2.getClassName()), "5-arg className");
        check("obj".equals(sr2.getObject()), "5-arg object");
        check(sr2.isRelative(), "5-arg relative true");
        check(sr2.getAttributes() == attrs, "5-arg attributes");

        // setAttributes
        BasicAttributes other = new BasicAttributes("sn", "Smith");
        sr.setAttributes(other);
        check(sr.getAttributes() == other, "setAttributes");

        // toString mentions name
        check(sr.toString().contains("cn=Alice"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
