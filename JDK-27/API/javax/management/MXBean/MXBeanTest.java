/*
 * @test
 * @summary javax.management.MXBean conformance: white-box structure + annotation behavior.
 * @run main MXBeanTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;
import javax.management.openmbean.*;

public class MXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MXBean");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == boolean.class, "value() returns boolean");
        check(Boolean.TRUE.equals(value.getDefaultValue()), "value() default true");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        MXBean ann = SampleMX.class.getAnnotation(MXBean.class);
        check(ann != null, "annotation present");
        check(ann.value(), "annotation value true");

        MXBean annFalse = SampleMXFalse.class.getAnnotation(MXBean.class);
        check(annFalse != null && !annFalse.value(), "annotation value false");
    }

    @MXBean
    public interface SampleMX { }
    @MXBean(false)
    public interface SampleMXFalse { }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMxAnnotationContract();
        testMxOpenTypeMetadata();
        testMxOpenConversion();
        testMxNotCompliant();
        testMxNamingConvention();
        System.out.println("PASS (" + passed + " assertions)");
    }

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

    // ---- deepened (batch e): annotation defaults, open-type metadata, real
    // open conversion loop (int[]/List/Map/enum), setter round-trip,
    // non-compliant MXBeans, naming convention vs @MXBean(false) ----

    public enum EColor { RED }

    @MXBean
    public static interface EPointMXBean {
        int getX();
        void setX(int x);
        int getY();
        List<String> getNames();
        Map<String,Integer> getScores();
        int[] getVec();
        EColor getColor();
    }

    public static class EPoint implements EPointMXBean {
        private int x = 3;
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public int getY() { return 4; }
        public List<String> getNames() { return Arrays.asList("a", "b"); }
        public Map<String,Integer> getScores() {
            LinkedHashMap<String,Integer> m = new LinkedHashMap<String,Integer>();
            m.put("one", Integer.valueOf(1));
            m.put("two", Integer.valueOf(2));
            return m;
        }
        public int[] getVec() { return new int[]{1, 2, 3}; }
        public EColor getColor() { return EColor.RED; }
    }

    public static class EBadPOJO { }

    @MXBean
    public static interface EBadMX { EBadPOJO getP(); }

    public static class EBadImpl implements EBadMX { public EBadPOJO getP() { return new EBadPOJO(); } }

    @MXBean(false)
    public static interface ENoSuffix { int getVal(); }

    public static class ENoSuffixImpl implements ENoSuffix { public int getVal() { return 1; } }

    public static interface EConvMXBean { List<String> getNames(); }

    public static class EConv implements EConvMXBean {
        public List<String> getNames() { return Arrays.asList("z"); }
    }

    @MXBean(false)
    public static interface EOffMXBean { List<String> getNames(); }

    public static class EOffImpl implements EOffMXBean {
        public List<String> getNames() { return Arrays.asList("z"); }
    }

    static void testMxAnnotationContract() throws Exception {
        Class<?> c = Class.forName("javax.management.MXBean");
        java.lang.reflect.Method value = c.getMethod("value");
        check(value.getReturnType() == boolean.class, "mx value() returns boolean");
        check(Boolean.TRUE.equals(value.getDefaultValue()), "mx value() defaults to true");
        check(EPointMXBean.class.getAnnotation(MXBean.class).value(), "mx @MXBean default value true");
        MXBean off = EOffMXBean.class.getAnnotation(MXBean.class);
        check(off != null && !off.value(), "mx @MXBean(false) value false");
    }

    static void testMxOpenTypeMetadata() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.mx", "type", "Point");
        server.registerMBean(new EPoint(), on);
        MBeanInfo info = server.getMBeanInfo(on);
        Descriptor d = info.getDescriptor();
        check("true".equals(String.valueOf(d.getFieldValue("mxbean"))), "mxbean=true in MBeanInfo descriptor");
        check(EPointMXBean.class.getName().equals(d.getFieldValue("interfaceClassName")),
                "interfaceClassName descriptor field");
        check("true".equals(String.valueOf(d.getFieldValue("immutableInfo"))), "immutableInfo descriptor field");
        MBeanAttributeInfo[] attrs = info.getAttributes();
        check(attrs.length == 6, "mx attribute count {X,Y,Names,Scores,Vec,Color}");
        String openNames = null, openVec = null, openScores = null, openColor = null, openX = null;
        String typeX = null, typeNames = null, typeVec = null, typeScores = null, typeColor = null;
        for (MBeanAttributeInfo a : attrs) {
            Object ot = a.getDescriptor().getFieldValue("openType");
            if ("X".equals(a.getName())) { openX = String.valueOf(ot); typeX = a.getType(); }
            else if ("Names".equals(a.getName())) { openNames = String.valueOf(ot); typeNames = a.getType(); }
            else if ("Vec".equals(a.getName())) { openVec = String.valueOf(ot); typeVec = a.getType(); }
            else if ("Scores".equals(a.getName())) { openScores = String.valueOf(ot); typeScores = a.getType(); }
            else if ("Color".equals(a.getName())) { openColor = String.valueOf(ot); typeColor = a.getType(); }
        }
        check("int".equals(typeX), "mx attr X mapped type string int");
        check("[Ljava.lang.String;".equals(typeNames), "mx attr Names mapped type [Ljava.lang.String;");
        check("[I".equals(typeVec), "mx attr Vec mapped type [I");
        check("javax.management.openmbean.TabularData".equals(typeScores), "mx attr Scores mapped type TabularData");
        check("java.lang.String".equals(typeColor), "mx attr Color mapped type String");
        check(openX != null && openX.contains("SimpleType(name=java.lang.Integer)"),
                "mx openType X is SimpleType INTEGER: " + openX);
        check(openNames != null && openNames.contains("primitiveArray=false"),
                "mx openType Names is non-primitive array: " + openNames);
        check(openVec != null && openVec.contains("primitiveArray=true"),
                "mx openType Vec is primitive array: " + openVec);
        check(openScores != null && openScores.contains("indexNames=(key)"),
                "mx openType Scores indexNames key: " + openScores);
        check(openColor != null && openColor.contains("SimpleType(name=java.lang.String)"),
                "mx openType Color is SimpleType STRING: " + openColor);
    }

    static void testMxOpenConversion() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.mx", "type", "Point");
        server.registerMBean(new EPoint(), on);
        Object vec = server.getAttribute(on, "Vec");
        check(vec instanceof int[], "mx int[] attribute is returned as int[] (open value)");
        check(vec.getClass().getName().equals("[I"), "mx Vec runtime class [I");
        int[] vecArr = (int[]) vec;
        check(vecArr.length == 3 && vecArr[0] == 1 && vecArr[1] == 2 && vecArr[2] == 3, "mx Vec contents preserved");
        Object names = server.getAttribute(on, "Names");
        check(names instanceof String[], "mx List<String> is converted to String[]");
        String[] namesArr = (String[]) names;
        check(namesArr.length == 2 && "a".equals(namesArr[0]) && "b".equals(namesArr[1]), "mx Names contents preserved");
        Object scores = server.getAttribute(on, "Scores");
        check(scores instanceof TabularData, "mx Map<String,Integer> is converted to TabularData");
        TabularData td = (TabularData) scores;
        check(td.getTabularType().getIndexNames().equals(Arrays.asList("key")), "mx tabular index name is key");
        check(td.getTabularType().getRowType().getType("key") != null
                && td.getTabularType().getRowType().getType("value") != null, "mx row type has key/value items");
        check(td.size() == 2, "mx tabular row count");
        Map<String,Integer> roundTripped = new HashMap<String,Integer>();
        for (Object row : td.values()) {
            CompositeData cd = (CompositeData) row;
            roundTripped.put((String) cd.get("key"), (Integer) cd.get("value"));
        }
        check(Integer.valueOf(1).equals(roundTripped.get("one")) && Integer.valueOf(2).equals(roundTripped.get("two")),
                "mx tabular key/value contents");
        check("RED".equals(server.getAttribute(on, "Color")), "mx enum attribute converted to its name String");
        check(Integer.valueOf(3).equals(server.getAttribute(on, "X")), "mx simple int attribute");
        check(Integer.valueOf(4).equals(server.getAttribute(on, "Y")), "mx readonly int attribute");
        server.setAttribute(on, new Attribute("X", Integer.valueOf(7)));
        check(Integer.valueOf(7).equals(server.getAttribute(on, "X")), "mx setX round-trip");
        final ObjectName target = on;
        checkThrows(InvalidAttributeValueException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(target, new Attribute("X", "str")); }
        }, "mx setAttribute wrong open value type");
    }

    static void testMxNotCompliant() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                server.registerMBean(new EBadImpl(), new ObjectName("jck.mx", "type", "Bad"));
            }
        }, "mx bean with untranslatable POJO return type rejected at registration");
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                server.registerMBean(new ENoSuffixImpl(), new ObjectName("jck.mx", "type", "NoSuffix"));
            }
        }, "@MXBean(false) without MXBean suffix follows standard introspection and fails");
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                server.registerMBean(new EOffImpl(), new ObjectName("jck.mx", "type", "Off"));
            }
        }, "@MXBean(false) with MXBean suffix also fails standard introspection");
    }

    static void testMxNamingConvention() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.mx", "type", "Conv");
        server.registerMBean(new EConv(), on);
        MBeanAttributeInfo a = server.getMBeanInfo(on).getAttributes()[0];
        check(a.getDescriptor().getFieldValue("openType") != null,
                "interface ending in MXBean is treated as MXBean without annotation");
        check("[Ljava.lang.String;".equals(a.getType()), "convention MXBean attribute open-mapped type");
        check(server.getAttribute(on, "Names") instanceof String[], "convention MXBean returns open value");
    }
}
