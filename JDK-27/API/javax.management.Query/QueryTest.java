/*
 * @test
 * @summary javax.management.Query conformance: JMX API type verification.
 * @run main QueryTest
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

public class QueryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public interface ThingMBean {
        int getCount();
        void setCount(int c);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    public static void main(String[] args) throws Exception {
        QueryExp e = Query.eq(Query.attr("Count"), Query.value(7));
        check(e != null, "eq non-null");
        check(Query.attr("Count").getAttributeName().equals("Count"), "attr name");
        check(Query.value("x") instanceof StringValueExp, "string value");
        check(Query.and(e, e) != null, "and");
        check(Query.or(e, e) != null, "or");
        check(Query.not(e) != null, "not");
        check(Query.gt(Query.attr("Count"), Query.value(1)) != null, "gt");
        check(Query.geq(Query.attr("Count"), Query.value(1)) != null, "geq");
        check(Query.leq(Query.attr("Count"), Query.value(1)) != null, "leq");
        check(Query.lt(Query.attr("Count"), Query.value(1)) != null, "lt");
        check(Query.between(Query.attr("Count"), Query.value(1), Query.value(10)) != null, "between");
        check(Query.match(Query.attr("Name"), Query.value("th*")) != null, "match");
        check(Query.attr("Count", "type") != null, "attr2");
        check(Query.classattr() != null, "classattr");
        check(Query.in(Query.attr("Count"), new ValueExp[]{Query.value(1), Query.value(2)}) != null, "in");
        check(Query.plus(Query.value(1), Query.value(2)) != null, "plus");
        check(Query.initialSubString(Query.attr("Name"), Query.value("th")) != null, "initialSubString");
        check(Query.anySubString(Query.attr("Name"), Query.value("in")) != null, "anySubString");
        check(Query.finalSubString(Query.attr("Name"), Query.value("ng")) != null, "finalSubString");
        check(Query.isInstanceOf(Query.value("java.lang.String")) != null, "isInstanceOf");
        check(Query.GT != Query.LT && Query.EQ != Query.PLUS, "constants");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=QueryTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        Set<ObjectName> res = mbs.queryNames(new ObjectName("test:*"), Query.eq(Query.attr("Count"), Query.value(7)));
        check(res.contains(on), "behavioral query");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
