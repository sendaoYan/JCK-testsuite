/*
 * @test
 * @summary javax.management.modelmbean.RequiredModelMBean conformance: white-box structure + black-box behavior.
 * @run main RequiredModelMBeanTest
 */

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotificationEmitter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.RequiredModelMBean;

public class RequiredModelMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.RequiredModelMBean");
        check(!c.isInterface(), "not interface");
        check(ModelMBean.class.isAssignableFrom(c), "implements ModelMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
        check(NotificationEmitter.class.isAssignableFrom(c), "implements NotificationEmitter");
        check(c.getConstructors().length >= 2, "has no-arg and ModelMBeanInfo constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RequiredModelMBean mb = new RequiredModelMBean();
        MBeanInfo defaultInfo = mb.getMBeanInfo();
        check(defaultInfo != null, "getMBeanInfo non-null");
        check(defaultInfo.getClassName() != null, "default info className non-null");

        ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
                "MyMBean", "desc",
                new ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new ModelMBeanOperationInfo[0],
                new ModelMBeanNotificationInfo[0]);
        mb.setModelMBeanInfo(info);
        check("MyMBean".equals(mb.getMBeanInfo().getClassName()),
                "setModelMBeanInfo reflected");

        // No attributes in the info -> getAttribute throws AttributeNotFoundException.
        boolean anfe = false;
        try {
            mb.getAttribute("Missing");
        } catch (AttributeNotFoundException e) {
            anfe = true;
        }
        check(anfe, "getAttribute on missing attribute throws AttributeNotFoundException");

        // Registration/unregistration lifecycle through a private MBeanServer.
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            ObjectName on = new ObjectName("test:type=RequiredModelMBeanTest");
            ObjectInstance registered = mbs.registerMBean(mb, on);
            check(on.equals(registered.getObjectName()), "registerMBean returns name");
            check(mbs.isRegistered(on), "isRegistered after register");
            mbs.unregisterMBean(on);
            check(!mbs.isRegistered(on), "not registered after unregister");
        } finally {
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }

    // ---- deepened (batch h): real MBeanServer routing closed loop ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** Managed resource with real state; routing must reach real methods on it. */
    public static class Resource {
        private int count = 3;
        private String label = "lab";
        public int getCount() { return count; }
        public void setCount(int v) { this.count = v; }
        public String getLabel() { return label; }
        public void setLabel(String l) { this.label = l; }
        public int add(int x) { return count + x; }
        public void reset() { count = 0; }
    }

    static ModelMBeanInfo buildInfo(Class<?> resClass, String attrCtl) throws Exception {
        String[] names = (attrCtl == null)
                ? new String[]{"name", "descriptorType", "getMethod", "setMethod"}
                : new String[]{"name", "descriptorType", "getMethod", "setMethod", "currencyTimeLimit"};
        Object[] values = (attrCtl == null)
                ? new Object[]{"Count", "attribute", "getCount", "setCount"}
                : new Object[]{"Count", "attribute", "getCount", "setCount", attrCtl};
        ModelMBeanAttributeInfo countAttr =
                new ModelMBeanAttributeInfo("Count", "int", "count attribute",
                true, true, false, new javax.management.modelmbean.DescriptorSupport(names, values));
        ModelMBeanOperationInfo[] ops = {
            new ModelMBeanOperationInfo("getCount op", resClass.getMethod("getCount")),
            new ModelMBeanOperationInfo("setCount op", resClass.getMethod("setCount", int.class)),
            new ModelMBeanOperationInfo("add op", resClass.getMethod("add", int.class)),
            new ModelMBeanOperationInfo("reset op", resClass.getMethod("reset"))};
        return new ModelMBeanInfoSupport(resClass.getName(), "resource model mbean",
                new ModelMBeanAttributeInfo[]{countAttr}, null, ops, null);
    }

    static void testTrueRoutingClosedLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        Resource res = new Resource();
        RequiredModelMBean mmb = new RequiredModelMBean();
        mmb.setManagedResource(res, "ObjectReference");
        mmb.setModelMBeanInfo(buildInfo(Resource.class, null));
        ObjectName on = new ObjectName("jck.mmb27:type=Routing");
        ObjectInstance oi = server.registerMBean(mmb, on);
        check(on.equals(oi.getObjectName()), "registerMBean returns ObjectInstance with name");
        // ObjectInstance class name comes from the ModelMBeanInfo className (DynamicMBean rule)
        check(Resource.class.getName().equals(oi.getClassName()),
                "registered class name is the managed resource class from ModelMBeanInfo");
        check(server.isRegistered(on), "isRegistered");
        // getAttribute routes to target.getCount() and returns its true value
        check(Integer.valueOf(3).equals(server.getAttribute(on, "Count")),
                "server.getAttribute(Count) routes to target.getCount() true value");
        check(res.getCount() == 3, "target state untouched by get");
        // setAttribute routes to target.setCount() and really mutates the target
        server.setAttribute(on, new Attribute("Count", 7));
        check(res.getCount() == 7, "server.setAttribute(Count,7) really changed target field to 7");
        check(Integer.valueOf(7).equals(server.getAttribute(on, "Count")), "get reflects new target value");
        // invoke routes to target.add(int) and returns its exact result
        Object ret = server.invoke(on, "add", new Object[]{5}, new String[]{"int"});
        check(Integer.valueOf(12).equals(ret), "server.invoke(add,5) routes to target.add returning 12");
        check(res.getCount() == 7, "add does not mutate count");
        // void operation routes and returns null
        Object vret = server.invoke(on, "reset", new Object[]{}, new String[]{});
        check(vret == null && res.getCount() == 0, "server.invoke(reset) routes to target.reset, returns null");
        // getMBeanInfo served by the server is the ModelMBeanInfo
        MBeanInfo info = server.getMBeanInfo(on);
        check(info instanceof ModelMBeanInfoSupport, "server.getMBeanInfo returns ModelMBeanInfoSupport");
        check(info.getAttributes().length == 1 && info.getAttributes()[0].getName().equals("Count"),
                "exposed attribute set from ModelMBeanInfo");
        check(info.getOperations().length == 4, "exposed operation count from ModelMBeanInfo");
        // batch getAttributes skips unknown names
        AttributeList got = server.getAttributes(on, new String[]{"Count", "NoSuch"});
        check(got.size() == 1 && Integer.valueOf(0).equals(((Attribute) got.get(0)).getValue()),
                "getAttributes returns only resolvable attributes");
        // batch setAttributes applies valid entries and skips unknown ones
        AttributeList set = new AttributeList();
        set.add(new Attribute("Count", 20));
        set.add(new Attribute("NoSuch", 1));
        AttributeList applied = server.setAttributes(on, set);
        check(applied.size() == 1 && res.getCount() == 20, "setAttributes applies valid, skips unknown");
        // unregister works through the MBeanRegistration implementation
        server.unregisterMBean(on);
        check(!server.isRegistered(on), "unregistered");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(on, "Count"); }
        }, "getAttribute after unregister");
    }

    static void testRoutingPrerequisitesAndNegatives() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        // attribute descriptor names getMethod but the operation is not declared -> invoke fails
        ModelMBeanAttributeInfo attr =
                new ModelMBeanAttributeInfo("Count", "int", "c", true, true, false,
                new javax.management.modelmbean.DescriptorSupport(
                        new String[]{"name", "descriptorType", "getMethod", "setMethod"},
                        new Object[]{"Count", "attribute", "getCount", "setCount"}));
        ModelMBeanOperationInfo addOp =
                new ModelMBeanOperationInfo("op", Resource.class.getMethod("add", int.class));
        ModelMBeanInfo noGetterOp = new ModelMBeanInfoSupport(Resource.class.getName(), "m",
                new ModelMBeanAttributeInfo[]{attr}, null,
                new ModelMBeanOperationInfo[]{addOp}, null);
        final RequiredModelMBean m1 = new RequiredModelMBean();
        m1.setManagedResource(new Resource(), "ObjectReference");
        m1.setModelMBeanInfo(noGetterOp);
        ObjectName on1 = new ObjectName("jck.mmb27:type=NoGetterOp");
        server.registerMBean(m1, on1);
        try {
            server.getAttribute(on1, "Count");
            check(false, "getAttribute with undeclared getMethod operation must throw");
        } catch (MBeanException me) {
            check(me.getCause() instanceof javax.management.ServiceNotFoundException,
                    "undeclared getMethod op -> MBeanException wrapping ServiceNotFoundException");
            check(me.getMessage() != null && me.getMessage().contains("getCount"),
                    "undeclared getMethod op message names the operation: " + me.getMessage());
        }
        // no managed resource set: routing cannot reach any target
        final RequiredModelMBean m2 = new RequiredModelMBean();
        m2.setModelMBeanInfo(buildInfo(Resource.class, null));
        ObjectName on2 = new ObjectName("jck.mmb27:type=NoResource");
        server.registerMBean(m2, on2);
        try {
            server.getAttribute(on2, "Count");
            check(false, "getAttribute without managed resource must throw");
        } catch (MBeanException me) {
            check(me.getCause() instanceof javax.management.ServiceNotFoundException,
                    "no managed resource -> MBeanException wrapping ServiceNotFoundException");
        }
        // invoke of an operation absent from ModelMBeanInfo (on1 declares only add)
        try {
            server.invoke(on1, "reset", new Object[]{}, new String[]{});
            check(false, "invoke of undeclared operation must throw");
        } catch (MBeanException me) {
            check(me.getCause() instanceof javax.management.ServiceNotFoundException
                    && me.getCause().getMessage() != null
                    && me.getCause().getMessage().contains("not in ModelMBeanInfo"),
                    "undeclared invoke: MBeanException wrapping ServiceNotFoundException naming the operation");
        }
        // unknown attribute
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(on2, "Nope"); }
        }, "getAttribute unknown attribute");
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(on2, new Attribute("Nope", 1)); }
        }, "setAttribute unknown attribute");
        // wrong value type: reflection failure inside the setMethod invocation (target present)
        RequiredModelMBean m4 = new RequiredModelMBean();
        m4.setManagedResource(new Resource(), "ObjectReference");
        m4.setModelMBeanInfo(buildInfo(Resource.class, null));
        ObjectName on4 = new ObjectName("jck.mmb27:type=WrongType");
        server.registerMBean(m4, on4);
        try {
            server.setAttribute(on4, new Attribute("Count", "notanint"));
            check(false, "setAttribute with wrong value type must throw");
        } catch (RuntimeOperationsException roe) {
            check(roe.getCause() instanceof IllegalArgumentException,
                    "wrong value type -> RuntimeOperationsException wrapping IllegalArgumentException");
        }
        server.unregisterMBean(on4);
        // operations with role="getter" route exactly like role="operation"
        ModelMBeanOperationInfo getterRole =
                new ModelMBeanOperationInfo("g", Resource.class.getMethod("getCount"),
                new javax.management.modelmbean.DescriptorSupport(
                        new String[]{"name", "descriptorType", "role"},
                        new Object[]{"getCount", "operation", "getter"}));
        ModelMBeanAttributeInfo roleAttr =
                new ModelMBeanAttributeInfo("Count", "int", "c", true, false, false,
                new javax.management.modelmbean.DescriptorSupport(
                        new String[]{"name", "descriptorType", "getMethod"},
                        new Object[]{"Count", "attribute", "getCount"}));
        ModelMBeanInfo roleInfo = new ModelMBeanInfoSupport(Resource.class.getName(), "m",
                new ModelMBeanAttributeInfo[]{roleAttr}, null,
                new ModelMBeanOperationInfo[]{getterRole}, null);
        RequiredModelMBean m3 = new RequiredModelMBean();
        Resource res3 = new Resource();
        res3.setCount(41);
        m3.setManagedResource(res3, "ObjectReference");
        m3.setModelMBeanInfo(roleInfo);
        ObjectName on3 = new ObjectName("jck.mmb27:type=RoleGetter");
        server.registerMBean(m3, on3);
        check(Integer.valueOf(41).equals(server.getAttribute(on3, "Count")),
                "operation with role=getter routes to the target method");
    }

    static void testInfoAndManagedResourceLifecycle() throws Exception {
        final RequiredModelMBean mmb = new RequiredModelMBean();
        // setModelMBeanInfo(null)
        try {
            mmb.setModelMBeanInfo(null);
            check(false, "setModelMBeanInfo(null) must throw");
        } catch (RuntimeOperationsException roe) {
            check(roe.getCause() instanceof IllegalArgumentException,
                    "setModelMBeanInfo(null) -> RuntimeOperationsException wrapping IllegalArgumentException");
        }
        // setModelMBeanInfo before registration is legal and clones the info
        ModelMBeanInfo info = buildInfo(Resource.class, null);
        mmb.setModelMBeanInfo(info);
        check(mmb.getMBeanInfo() instanceof ModelMBeanInfoSupport, "setModelMBeanInfo stores a ModelMBeanInfo");
        check(mmb.getMBeanInfo().getAttributes().length == 1, "stored info keeps attribute");
        // setModelMBeanInfo while registered is forbidden
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.mmb27:type=Lifecycle");
        server.registerMBean(mmb, on);
        try {
            mmb.setModelMBeanInfo(info);
            check(false, "setModelMBeanInfo while registered must throw");
        } catch (RuntimeOperationsException roe) {
            check(roe.getCause() instanceof IllegalStateException,
                    "setModelMBeanInfo while registered -> RuntimeOperationsException wrapping IllegalStateException");
        }
        // setManagedResource target type matrix (case-insensitive "objectReference" only)
        Resource res = new Resource();
        mmb.setManagedResource(res, "ObjectReference");
        mmb.setManagedResource(res, "objectReference");
        mmb.setManagedResource(res, "OBJECTREFERENCE");
        check(true, "target type objectReference accepted case-insensitively");
        try {
            mmb.setManagedResource(res, "Bogus");
            check(false, "setManagedResource with bogus type must throw");
        } catch (InvalidTargetObjectTypeException itote) {
            check(itote.getMessage() != null && itote.getMessage().contains("Bogus"),
                    "bogus target type message contains the value: " + itote.getMessage());
        }
        checkThrows(InvalidTargetObjectTypeException.class, new ThrowingRunnable() {
            public void run() throws Exception { mmb.setManagedResource(new Resource(), null); }
        }, "setManagedResource null target type");
        // persistence is not supported by RequiredModelMBean
        try {
            mmb.load();
            check(false, "load must throw");
        } catch (MBeanException me) {
            check(me.getCause() instanceof javax.management.ServiceNotFoundException,
                    "load -> MBeanException wrapping ServiceNotFoundException");
        }
        try {
            mmb.store();
            check(false, "store must throw");
        } catch (MBeanException me) {
            check(me.getCause() instanceof javax.management.ServiceNotFoundException,
                    "store -> MBeanException wrapping ServiceNotFoundException");
        }
        server.unregisterMBean(on);
    }

    static void testFreshDefaults() throws Exception {
        RequiredModelMBean fresh = new RequiredModelMBean();
        MBeanInfo info = fresh.getMBeanInfo();
        check(info != null, "fresh RequiredModelMBean has a default ModelMBeanInfo");
        check("Default ModelMBean".equals(info.getDescription()), "default description");
        check(info.getAttributes().length == 0 && info.getOperations().length == 0
                && info.getConstructors().length == 0 && info.getNotifications().length == 0,
                "default info exposes nothing");
        // MBean registration lifecycle is delegated to the built-in MBeanRegistration implementation
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.mmb27:type=Fresh");
        server.registerMBean(fresh, on);
        check(server.getMBeanInfo(on) != null, "fresh instance registers and serves info");
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { fresh.getAttribute("Any"); }
        }, "fresh getAttribute without info");
        // default notification support: GENERIC + ATTRIBUTE_CHANGE entries
        MBeanNotificationInfo[] nis = fresh.getNotificationInfo();
        check(nis.length == 2, "default getNotificationInfo has 2 entries");
        java.util.Set<String> types = new java.util.HashSet<String>();
        for (MBeanNotificationInfo ni : nis)
            for (String t : ni.getNotifTypes()) types.add(t);
        check(types.contains("jmx.modelmbean.generic"), "default GENERIC notification type");
        check(types.contains("jmx.attribute.change"), "default ATTRIBUTE_CHANGE notification type");
        for (MBeanNotificationInfo ni : nis) {
            if ("ATTRIBUTE_CHANGE".equals(ni.getName())) {
                check("T".equals(ni.getDescriptor().getFieldValue("log")), "default ACN log=T");
                check("6".equals(String.valueOf(ni.getDescriptor().getFieldValue("severity"))),
                        "default ACN severity=6");
            }
        }
        server.unregisterMBean(on);
    }

    static void testCachingSemantics() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        // currencyTimeLimit=0: value is cached forever once fetched; later target changes are invisible
        Resource sticky = new Resource();
        RequiredModelMBean m0 = new RequiredModelMBean();
        m0.setManagedResource(sticky, "ObjectReference");
        m0.setModelMBeanInfo(buildInfo(Resource.class, "0"));
        ObjectName on0 = new ObjectName("jck.mmb27:type=Cache0");
        server.registerMBean(m0, on0);
        check(Integer.valueOf(3).equals(server.getAttribute(on0, "Count")), "ctl=0 first get returns target value");
        sticky.setCount(99);
        check(Integer.valueOf(3).equals(server.getAttribute(on0, "Count")),
                "ctl=0: after target changes to 99 the cached value 3 is still returned (sticky)");
        // currencyTimeLimit=-1: never cached; every get re-invokes the getMethod
        Resource live = new Resource();
        RequiredModelMBean mf = new RequiredModelMBean();
        mf.setManagedResource(live, "ObjectReference");
        mf.setModelMBeanInfo(buildInfo(Resource.class, "-1"));
        ObjectName onf = new ObjectName("jck.mmb27:type=CacheFresh");
        server.registerMBean(mf, onf);
        check(Integer.valueOf(3).equals(server.getAttribute(onf, "Count")), "ctl=-1 first get returns target value");
        live.setCount(42);
        check(Integer.valueOf(42).equals(server.getAttribute(onf, "Count")),
                "ctl=-1: target change to 42 is visible immediately (never cached)");
        // no getMethod in descriptor: getAttribute returns the descriptor's value/default field
        ModelMBeanAttributeInfo dv =
                new ModelMBeanAttributeInfo("Label", "java.lang.String", "l", true, true, false,
                new javax.management.modelmbean.DescriptorSupport(
                        new String[]{"name", "descriptorType", "value"},
                        new Object[]{"Label", "attribute", "storedLabel"}));
        ModelMBeanInfo dvi = new ModelMBeanInfoSupport(Resource.class.getName(), "m",
                new ModelMBeanAttributeInfo[]{dv}, null, null, null);
        RequiredModelMBean md = new RequiredModelMBean();
        md.setManagedResource(new Resource(), "ObjectReference");
        md.setModelMBeanInfo(dvi);
        ObjectName ond = new ObjectName("jck.mmb27:type=DefVal");
        server.registerMBean(md, ond);
        check("storedLabel".equals(server.getAttribute(ond, "Label")),
                "no getMethod: getAttribute returns descriptor value field");
    }

    static void testNotificationPassthrough() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        RequiredModelMBean mmb = new RequiredModelMBean();
        ModelMBeanNotificationInfo declared =
                new ModelMBeanNotificationInfo(
                new String[]{"jck.custom"}, "JckNotif", "custom notif");
        ModelMBeanInfo info = new ModelMBeanInfoSupport(Resource.class.getName(), "m",
                null, null, null, new ModelMBeanNotificationInfo[]{declared});
        mmb.setModelMBeanInfo(info);
        mmb.setManagedResource(new Resource(), "ObjectReference");
        ObjectName on = new ObjectName("jck.mmb27:type=Notif");
        server.registerMBean(mmb, on);
        // declared notification info is merged with the two defaults
        MBeanNotificationInfo[] served = mmb.getNotificationInfo();
        check(served.length == 3, "declared + 2 default notification entries: " + served.length);
        java.util.Set<String> all = new java.util.HashSet<String>();
        for (MBeanNotificationInfo ni : served) all.add(ni.getName());
        check(all.contains("JckNotif") && all.contains("GENERIC") && all.contains("ATTRIBUTE_CHANGE"),
                "notification names contain declared plus defaults");
        // declaring GENERIC yourself suppresses the auto-added duplicate
        ModelMBeanNotificationInfo gen =
                new ModelMBeanNotificationInfo(
                new String[]{"jmx.modelmbean.generic"}, "GENERIC", "d");
        RequiredModelMBean m2 = new RequiredModelMBean();
        m2.setModelMBeanInfo(new ModelMBeanInfoSupport(Resource.class.getName(), "m",
                null, null, null, new ModelMBeanNotificationInfo[]{gen}));
        check(m2.getNotificationInfo().length == 2, "declared GENERIC not duplicated");
        // listener receives the very same Notification instance from sendNotification
        final java.util.List<Notification> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<Notification>());
        final Object handback = new Object();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { got.add(n); check(h == handback, "handback delivered unchanged"); }
        };
        mmb.addNotificationListener(listener, null, handback);
        Notification sent = new Notification("jck.custom", mmb, 1L, "hello");
        mmb.sendNotification(sent);
        check(got.size() == 1 && got.get(0) == sent, "sendNotification delivers the same instance to listener");
        // sendNotification(String) builds a generic notification
        mmb.sendNotification("text-msg");
        check(got.size() == 2, "sendNotification(String) delivered");
        Notification generic = got.get(1);
        check("jmx.modelmbean.generic".equals(generic.getType()), "sendNotification(String) type is jmx.modelmbean.generic");
        check("text-msg".equals(generic.getMessage()), "sendNotification(String) message is the text");
        check(generic.getSource() == mmb, "sendNotification(String) source is the MBean");
        // negative notification inputs
        checkThrows(RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { mmb.sendNotification((Notification) null); }
        }, "sendNotification(null Notification)");
        checkThrows(RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { mmb.sendNotification((String) null); }
        }, "sendNotification(null String)");
        mmb.sendNotification("");
        check(got.size() == 3, "sendNotification(\"\") accepted (delivers empty text)");
        // sendAttributeChangeNotification(Attribute, Attribute) builds an AttributeChangeNotification
        got.clear();
        mmb.sendAttributeChangeNotification(new Attribute("Count", 7), new Attribute("Count", 9));
        check(got.size() == 1, "two-Attribute send delivers to plain listeners as well");
        AttributeChangeNotification acn = (AttributeChangeNotification) got.get(0);
        check("jmx.attribute.change".equals(acn.getType()), "ACN type jmx.attribute.change");
        check("Count".equals(acn.getAttributeName()), "ACN attributeName");
        check(Integer.valueOf(7).equals(acn.getOldValue()) && Integer.valueOf(9).equals(acn.getNewValue()),
                "ACN old/new values");
        // setAttribute emits an attribute change notification automatically
        RequiredModelMBean m3 = new RequiredModelMBean();
        m3.setManagedResource(new Resource(), "ObjectReference");
        m3.setModelMBeanInfo(buildInfo(Resource.class, null));
        ObjectName on3 = new ObjectName("jck.mmb27:type=AcnSet");
        server.registerMBean(m3, on3);
        got.clear();
        final Notification[] box = new Notification[1];
        NotificationListener oneShot = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { box[0] = n; }
        };
        m3.addAttributeChangeNotificationListener(oneShot, "Count", null);
        server.setAttribute(on3, new Attribute("Count", 13));
        check(box[0] instanceof AttributeChangeNotification, "setAttribute triggers AttributeChangeNotification");
        check("Count".equals(((AttributeChangeNotification) box[0]).getAttributeName()),
                "auto ACN names the attribute");
        // removing unknown listeners
        final NotificationListener unknown = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { }
        };
        checkThrows(javax.management.ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mmb.removeNotificationListener(unknown); }
        }, "removeNotificationListener unknown");
        checkThrows(javax.management.ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mmb.removeAttributeChangeNotificationListener(unknown, "Count"); }
        }, "removeAttributeChangeNotificationListener unknown");
        server.unregisterMBean(on);
        server.unregisterMBean(on3);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTrueRoutingClosedLoop();
        testRoutingPrerequisitesAndNegatives();
        testInfoAndManagedResourceLifecycle();
        testFreshDefaults();
        testCachingSemantics();
        testNotificationPassthrough();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
