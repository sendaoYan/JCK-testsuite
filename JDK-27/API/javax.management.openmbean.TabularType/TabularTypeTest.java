/*
 * @test
 * @summary javax.management.openmbean.TabularType conformance: JMX API type verification.
 * @run main TabularTypeTest
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

public class TabularTypeTest {
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
        check(tt.getTypeName().equals("MyTab"), "typeName");
        check(tt.getRowType() == ct, "rowType");
        check(tt.getIndexNames().size() == 1, "indexNames size");
        check(tt.getIndexNames().get(0).equals("name"), "index name");
        check(tt.getClassName().equals("javax.management.openmbean.TabularData"), "className");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
