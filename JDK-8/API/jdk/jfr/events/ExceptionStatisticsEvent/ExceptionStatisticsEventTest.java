/*
 * @test
 * @summary jdk.jfr.events.ExceptionStatisticsEvent conformance: type shape via reflection (headless-safe).
 * @run main ExceptionStatisticsEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ExceptionStatisticsEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.ExceptionStatisticsEvent", false, ExceptionStatisticsEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.events.AbstractJDKEvent"), "superclass jdk.jfr.events.AbstractJDKEvent");
        check(hasField(c, "throwables"), "declares field throwables");

        // ===================== 深化 (2026-11-05): 运行时事件类型元数据闭环 =====================
        // 事件类 -> FlightRecorder 注册的 jdk.ExceptionStatistics: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.ExceptionStatistics".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.ExceptionStatistics");
        check("jdk.ExceptionStatistics".equals(evt.getName()), "getName == jdk.ExceptionStatistics");
        check("Exception Statistics".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Java Application", "Statistics")), "categoryNames 精确匹配");
        check("Number of objects derived from java.lang.Throwable that have been created".equals(evt.getDescription()), "getDescription 精确匹配");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "duration", "eventThread", "stackTrace", "throwables")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.ExceptionStatistics".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad != null && "Number of objects derived from java.lang.Throwable that have been created".equals(ad.value()), "类 @Description == 类型 description");
        // ---- 用户字段: 类字段 @Label/类型 与 ValueDescriptor 一致 (含父类字段) ----
        java.util.Map<String, String> fieldLabel = new java.util.HashMap<String, String>();
        java.util.Map<String, String> fieldType = new java.util.HashMap<String, String>();
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            for (java.lang.reflect.Field cf : k.getDeclaredFields()) {
                jdk.jfr.Label cfl = cf.getAnnotation(jdk.jfr.Label.class);
                if (cfl != null) { fieldLabel.put(cf.getName(), cfl.value()); fieldType.put(cf.getName(), cf.getType().getSimpleName()); }
            }
        }
        for (jdk.jfr.ValueDescriptor vd : evt.getFields()) {
            String fn = vd.getName();
            if (fn.equals("startTime") || fn.equals("duration") || fn.equals("eventThread") || fn.equals("stackTrace")) continue;
            String wl = fieldLabel.get(fn);
            check(wl != null && wl.equals(vd.getLabel()), "字段标签一致: " + fn);
            check(vd.getAnnotation(jdk.jfr.Label.class) != null && wl.equals(vd.getAnnotation(jdk.jfr.Label.class).value()), "字段描述符 @Label: " + fn);
            check(vd.getTypeName().endsWith(fieldType.get(fn)), "字段类型一致: " + fn + " -> " + vd.getTypeName());
        }

        // ---- 版本差异锁定: JDK-27 独有事件类型在本 JDK(8) 缺席 (enable() 对未知名不校验, 必须用名册断言) ----
        java.util.Set<String> allNames = new java.util.HashSet<String>();
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) allNames.add(cand.getName());
        check(!allNames.contains("jdk.ObjectAllocationSample"), "jdk.ObjectAllocationSample 在 JDK-8 缺席");
        check(!allNames.contains("jdk.VirtualThreadStart"), "jdk.VirtualThreadStart 在 JDK-8 缺席");
        check(!allNames.contains("jdk.X509Certificate"), "jdk.X509Certificate 在 JDK-8 缺席");
        check(!allNames.contains("jdk.TLSHandshake"), "jdk.TLSHandshake 在 JDK-8 缺席");
        check(!allNames.contains("jdk.ContainerCPUUsage"), "jdk.ContainerCPUUsage 在 JDK-8 缺席");

        // ===================== 深化: 录制-回读闭环 (period=1s 周期事件, 1.3s 窗口内必触发) =====================
        java.nio.file.Path stDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            jdk.jfr.Recording stRec = new jdk.jfr.Recording();
            stRec.enable("jdk.ExceptionStatistics").with("period", "1 s");
            stRec.start();
            Thread.sleep(1300);
            stRec.stop();
            stRec.dump(stDir.resolve("dump.jfr"));
            stRec.close();
            int stCnt = 0; long stThrowables = -1;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(stDir.resolve("dump.jfr"))) {
                if (!"jdk.ExceptionStatistics".equals(e.getEventType().getName())) continue;
                stCnt++;
                stThrowables = e.getLong("throwables");
                check(e.getStartTime() != null, "统计事件 startTime 非空");
            }
            check(stCnt >= 1, "1.3s 窗口内 jdk.ExceptionStatistics >= 1 条, 实得 " + stCnt);
            check(stThrowables >= 0, "throwables >= 0, 实得 " + stThrowables);
        } finally {
            try { java.nio.file.Files.deleteIfExists(stDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(stDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(stDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(stDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
