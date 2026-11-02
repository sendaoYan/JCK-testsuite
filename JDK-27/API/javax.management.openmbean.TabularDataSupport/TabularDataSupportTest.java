/*
 * @test
 * @summary javax.management.openmbean.TabularDataSupport conformance: JMX API type verification.
 * @run main TabularDataSupportTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.security.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.security.auth.Subject;
import javax.management.*;
import javax.management.loading.*;
import javax.management.modelmbean.*;
import javax.management.monitor.*;
import javax.management.openmbean.*;
import javax.management.relation.*;
import javax.management.remote.*;
import javax.management.remote.rmi.*;
import javax.management.timer.*;

public class TabularDataSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String[] names = {"name", "value"};
        String[] descs = {"name desc", "value desc"};
        OpenType<?>[] types = {SimpleType.STRING, SimpleType.INTEGER};
        CompositeType ct = new CompositeType("MyType", "desc", names, descs, types);
        TabularType tt = new TabularType("MyTab", "desc", ct, new String[]{"name"});
        TabularDataSupport td = new TabularDataSupport(tt);
        CompositeDataSupport row = new CompositeDataSupport(ct, names, new Object[]{"n", 5});
        td.put(row);
        check(td.size() == 1, "size");
        check(td.containsKey(td.calculateIndex(row)), "containsKey");
        check(td.get(td.calculateIndex(row)).equals(row), "get");
        check(!td.isEmpty(), "not empty");
        check(td.getTabularType().equals(tt), "tabular type");
        check(td.values().size() == 1, "values");
        td.remove(td.calculateIndex(row));
        check(td.isEmpty(), "empty after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
