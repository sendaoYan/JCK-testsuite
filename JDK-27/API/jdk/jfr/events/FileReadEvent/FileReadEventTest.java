/*
 * @test
 * @summary jdk.jfr.events.FileReadEvent conformance: type shape.
 * @run main FileReadEventTest
 */

import java.lang.reflect.Modifier;

public class FileReadEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.FileReadEvent");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.internal.MirrorEvent"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");

        // ===================== 深化 (2026-11-05): 运行时事件类型元数据闭环 =====================
        // 事件类 -> FlightRecorder 注册的 jdk.FileRead: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.FileRead".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.FileRead");
        check("jdk.FileRead".equals(evt.getName()), "getName == jdk.FileRead");
        check("File Read".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Java Application")), "categoryNames 精确匹配");
        check("Reading data from a file".equals(evt.getDescription()), "getDescription 精确匹配");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "duration", "eventThread", "stackTrace", "path", "bytesRead", "endOfFile")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.FileRead".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad != null && "Reading data from a file".equals(ad.value()), "类 @Description == 类型 description");
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

        // ===================== 深化: 录制-回读闭环 (FileInputStream 两次 4 字节读触发 jdk.FileRead) =====================
        java.nio.file.Path rdDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            java.nio.file.Files.write(rdDir.resolve("data.bin"), new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            jdk.jfr.Recording rdRec = new jdk.jfr.Recording();
            rdRec.enable("jdk.FileRead").with("threshold", "0 ns");
            rdRec.start();
            java.io.FileInputStream rdIn = new java.io.FileInputStream(rdDir.resolve("data.bin").toFile());
            byte[] rdBuf = new byte[4];
            int rdN1 = rdIn.read(rdBuf);
            int rdN2 = rdIn.read(rdBuf);
            rdIn.close();
            rdRec.stop();
            rdRec.dump(rdDir.resolve("dump.jfr"));
            rdRec.close();
            check(rdN1 == 4 && rdN2 == 4, "两次 read 各返回 4 字节");
            int rdCnt = 0; long rdBytes = 0; boolean rdEof = false; long rdDur = -1;
            java.util.List<String> rdPaths = new java.util.ArrayList<>();
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(rdDir.resolve("dump.jfr"))) {
                if (!"jdk.FileRead".equals(e.getEventType().getName())) continue;
                if (!rdDir.resolve("data.bin").toAbsolutePath().toString().equals(e.getString("path"))) continue;
                rdCnt++;
                rdBytes += e.getLong("bytesRead");
                rdEof = rdEof || e.getBoolean("endOfFile");
                rdDur = e.getDuration().toNanos();
                rdPaths.add(String.valueOf(e.getString("path")));
                check(e.getThread() != null && Thread.currentThread().getName().equals(e.getThread().getJavaName()), "FileRead eventThread 为当前线程(与线程名自洽, 免 harness 命名差异)");
            }
            check(rdCnt == 2, "jdk.FileRead 恰 2 条(两次 read), 实得 " + rdCnt);
            check(rdBytes == 8, "bytesRead 总和 == 8, 实得 " + rdBytes);
            check(!rdEof, "未读至文件尾, endOfFile=false");
            check(rdDur >= 0, "duration(ns) >= 0");
            check(rdPaths.size() == 2 && rdPaths.get(0).equals(rdPaths.get(1)), "path 字段为绝对路径且一致");
        } finally {
            try { java.nio.file.Files.deleteIfExists(rdDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(rdDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(rdDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(rdDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
