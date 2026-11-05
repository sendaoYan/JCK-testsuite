/*
 * @test
 * @summary jdk.jfr.events.FileWriteEvent conformance: type shape via reflection (headless-safe).
 * @run main FileWriteEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FileWriteEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.FileWriteEvent", false, FileWriteEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.events.AbstractJDKEvent"), "superclass jdk.jfr.events.AbstractJDKEvent");
        check(hasMethod(c, "reset"), "declares reset");
        check(hasField(c, "EVENT"), "declares field EVENT");
        check(hasField(c, "bytesWritten"), "declares field bytesWritten");
        check(hasField(c, "path"), "declares field path");

        // ===================== 深化 (2026-11-05): 运行时事件类型元数据闭环 =====================
        // 事件类 -> FlightRecorder 注册的 jdk.FileWrite: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.FileWrite".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.FileWrite");
        check("jdk.FileWrite".equals(evt.getName()), "getName == jdk.FileWrite");
        check("File Write".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Java Application")), "categoryNames 精确匹配");
        check("Writing data to a file".equals(evt.getDescription()), "getDescription 精确匹配");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "duration", "eventThread", "stackTrace", "path", "bytesWritten")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.FileWrite".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad != null && "Writing data to a file".equals(ad.value()), "类 @Description == 类型 description");
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

        // ===================== 深化: 录制-回读闭环 (FileOutputStream 写 5 字节触发 jdk.FileWrite) =====================
        java.nio.file.Path wrDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            jdk.jfr.Recording wrRec = new jdk.jfr.Recording();
            wrRec.enable("jdk.FileWrite").with("threshold", "0 ns");
            wrRec.start();
            java.io.FileOutputStream wrOut = new java.io.FileOutputStream(wrDir.resolve("w.bin").toFile());
            wrOut.write(new byte[]{9, 8, 7, 6, 5});
            wrOut.flush();
            wrOut.close();
            wrRec.stop();
            wrRec.dump(wrDir.resolve("dump.jfr"));
            wrRec.close();
            int wrCnt = 0; long wrBytes = 0;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(wrDir.resolve("dump.jfr"))) {
                if (!"jdk.FileWrite".equals(e.getEventType().getName())) continue;
                if (!wrDir.resolve("w.bin").toAbsolutePath().toString().equals(e.getString("path"))) continue;
                wrCnt++;
                wrBytes += e.getLong("bytesWritten");
                check(e.getDuration().toNanos() >= 0, "FileWrite duration >= 0");
                check(e.getThread() != null && Thread.currentThread().getName().equals(e.getThread().getJavaName()), "FileWrite eventThread 为当前线程");
            }
            check(wrCnt == 1, "jdk.FileWrite 恰 1 条(按 path 过滤), 实得 " + wrCnt);
            check(wrBytes == 5, "bytesWritten == 5, 实得 " + wrBytes);
        } finally {
            try { java.nio.file.Files.deleteIfExists(wrDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(wrDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(wrDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(wrDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
