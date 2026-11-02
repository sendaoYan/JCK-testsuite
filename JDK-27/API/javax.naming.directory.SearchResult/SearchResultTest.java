/*
 * @test
 * @summary javax.naming.directory.SearchResult conformance: API type verification.
 * @run main SearchResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SearchResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes as = new javax.naming.directory.BasicAttributes("cn", "value");
javax.naming.directory.SearchResult sr = new javax.naming.directory.SearchResult("name", "object", as);
check(sr.getName().equals("name"), "name");
check("object".equals(sr.getObject()), "object");
check(sr.getAttributes() == as, "attributes");
check(sr instanceof javax.naming.Binding, "Binding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
