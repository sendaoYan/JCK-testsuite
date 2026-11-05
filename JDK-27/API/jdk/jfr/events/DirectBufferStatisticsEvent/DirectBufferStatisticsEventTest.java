/*
 * @test
 * @summary jdk.jfr.events.DirectBufferStatisticsEvent conformance: type shape.
 * @run main DirectBufferStatisticsEventTest
 */

import java.lang.reflect.Modifier;

public class DirectBufferStatisticsEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.DirectBufferStatisticsEvent");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.events.AbstractBufferStatisticsEvent"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");

        // ===================== 深化 (2026-11-05): 运行时事件类型元数据闭环 =====================
        // 事件类 -> FlightRecorder 注册的 jdk.DirectBufferStatistics: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.DirectBufferStatistics".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.DirectBufferStatistics");
        check("jdk.DirectBufferStatistics".equals(evt.getName()), "getName == jdk.DirectBufferStatistics");
        check("Direct Buffer Statistics".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Java Application", "Statistics")), "categoryNames 精确匹配");
        check("Statistics of direct buffer".equals(evt.getDescription()), "getDescription 精确匹配");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "maxCapacity", "count", "totalCapacity", "memoryUsed")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.DirectBufferStatistics".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad != null && "Statistics of direct buffer".equals(ad.value()), "类 @Description == 类型 description");
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

        // ---- 版本差异锁定: JDK-27 独有事件类型在册 ----
        java.util.Set<String> allNames = new java.util.HashSet<String>();
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) allNames.add(cand.getName());
        check(allNames.contains("jdk.ObjectAllocationSample"), "jdk.ObjectAllocationSample 在 JDK-27 在册");
        check(allNames.contains("jdk.VirtualThreadPinned"), "jdk.VirtualThreadPinned 在 JDK-27 在册");

        // ===================== 深化: 录制-回读闭环 (period=1s 的 jdk.DirectBufferStatistics 周期事件) =====================
        java.nio.file.Path dbDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            jdk.jfr.Recording dbRec = new jdk.jfr.Recording();
            dbRec.enable("jdk.DirectBufferStatistics").with("period", "1 s");
            dbRec.start();
            Thread.sleep(1300);
            dbRec.stop();
            dbRec.dump(dbDir.resolve("dump.jfr"));
            dbRec.close();
            int dbCnt = 0;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(dbDir.resolve("dump.jfr"))) {
                if (!"jdk.DirectBufferStatistics".equals(e.getEventType().getName())) continue;
                dbCnt++;
                check(e.getLong("maxCapacity") > 0, "maxCapacity > 0 (物理内存上界)");
                check(e.getLong("count") >= 0, "count >= 0");
                check(e.getLong("totalCapacity") >= 0, "totalCapacity >= 0");
                check(e.getLong("memoryUsed") >= 0, "memoryUsed >= 0");
            }
            check(dbCnt >= 1, "1.3s 窗口内 jdk.DirectBufferStatistics >= 1 条, 实得 " + dbCnt);
        } finally {
            try { java.nio.file.Files.deleteIfExists(dbDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dbDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dbDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dbDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
