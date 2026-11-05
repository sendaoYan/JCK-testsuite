/*
 * @test
 * @summary jdk.jfr.events.ActiveSettingEvent conformance: type shape via reflection (headless-safe).
 * @run main ActiveSettingEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ActiveSettingEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.ActiveSettingEvent", false, ActiveSettingEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.events.AbstractJDKEvent"), "superclass jdk.jfr.events.AbstractJDKEvent");
        check(hasField(c, "id"), "declares field id");
        check(hasField(c, "name"), "declares field name");
        check(hasField(c, "value"), "declares field value");

        // ===================== 深化 (2026-11-05): 运行时事件类型元数据闭环 =====================
        // 事件类 -> FlightRecorder 注册的 jdk.ActiveSetting: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.ActiveSetting".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.ActiveSetting");
        check("jdk.ActiveSetting".equals(evt.getName()), "getName == jdk.ActiveSetting");
        check("Recording Setting".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Flight Recorder")), "categoryNames 精确匹配");
        check(evt.getDescription() == null, "getDescription == null");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "duration", "eventThread", "stackTrace", "id", "name", "value")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.ActiveSetting".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad == null, "类无 @Description 与类型 description==null 一致");
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

        // ===================== 深化: 录制-回读闭环 (设置快照事件 jdk.ActiveSetting: 本录制对各事件类型的生效设置) =====================
        java.nio.file.Path asDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            jdk.jfr.Recording asRec = new jdk.jfr.Recording();
            asRec.enable("jdk.ActiveSetting");
            asRec.enable("jdk.FileRead").with("threshold", "0 ns");
            asRec.start();
            asRec.stop();
            asRec.dump(asDir.resolve("dump.jfr"));
            asRec.close();
            jdk.jfr.EventType asFr = null;
            for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
                if ("jdk.FileRead".equals(cand.getName())) { asFr = cand; break; }
            }
            check(asFr != null, "定位 jdk.FileRead 类型 id");
            long asFrId = asFr == null ? -1L : asFr.getId();
            int asTotal = 0; int asThr = 0; int asEna = 0;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(asDir.resolve("dump.jfr"))) {
                if (!"jdk.ActiveSetting".equals(e.getEventType().getName())) continue;
                asTotal++;
                check(e.getLong("id") > 0, "ActiveSetting.id > 0");
                check(e.getString("name") != null, "ActiveSetting.name 非空");
                check(e.getString("value") != null, "ActiveSetting.value 非空");
                if (e.getLong("id") == asFrId) {
                    if ("threshold".equals(e.getString("name"))) { asThr++; check("0 ns".equals(e.getString("value")), "jdk.FileRead threshold 回读 == 0 ns"); }
                    if ("enabled".equals(e.getString("name"))) { asEna++; check("true".equals(e.getString("value")), "jdk.FileRead enabled 回读 == true"); }
                }
            }
            check(asTotal >= 1, "jdk.ActiveSetting >= 1 条, 实得 " + asTotal);
            check(asThr >= 1, "存在 id==jdk.FileRead 且 name==threshold 的快照行");
            check(asEna >= 1, "存在 id==jdk.FileRead 且 name==enabled=true 的快照行");
        } finally {
            try { java.nio.file.Files.deleteIfExists(asDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(asDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(asDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(asDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
