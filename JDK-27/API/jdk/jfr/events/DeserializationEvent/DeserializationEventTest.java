/*
 * @test
 * @summary jdk.jfr.events.DeserializationEvent conformance: type shape.
 * @run main DeserializationEventTest
 */

import java.lang.reflect.Modifier;

public class DeserializationEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.DeserializationEvent");
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
        // 事件类 -> FlightRecorder 注册的 jdk.Deserialization: name/label/category/description/字段全表 (实测基线见 DESIGN.md)
        jdk.jfr.EventType evt = null;
        for (jdk.jfr.EventType cand : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if ("jdk.Deserialization".equals(cand.getName())) { evt = cand; break; }
        }
        check(evt != null, "运行时事件类型在册: jdk.Deserialization");
        check("jdk.Deserialization".equals(evt.getName()), "getName == jdk.Deserialization");
        check("Deserialization".equals(evt.getLabel()), "getLabel 精确匹配");
        java.util.List<String> evtCats = evt.getCategoryNames();
        check(evtCats.equals(java.util.Arrays.asList("Java Development Kit", "Serialization")), "categoryNames 精确匹配");
        check("Results of deserialization and ObjectInputFilter checks".equals(evt.getDescription()), "getDescription 精确匹配");
        check(evt.getId() > 0, "getId() > 0");
        check(!evt.isEnabled(), "无录制启用时 isEnabled == false");
        java.util.List<String> evtFields = new java.util.ArrayList<String>();
        for (jdk.jfr.ValueDescriptor vd0 : evt.getFields()) evtFields.add(vd0.getName());
        check(evtFields.equals(java.util.Arrays.asList("startTime", "eventThread", "stackTrace", "filterConfigured", "filterStatus", "type", "arrayLength", "objectReferences", "depth", "bytesRead", "exceptionType", "exceptionMessage")), "字段名序列精确匹配");
        // ---- 类注解 <-> 运行时元数据 一致性 ----
        jdk.jfr.Name an = c.getAnnotation(jdk.jfr.Name.class);
        check(an != null && "jdk.Deserialization".equals(an.value()), "类 @Name == 类型名");
        jdk.jfr.Label al = c.getAnnotation(jdk.jfr.Label.class);
        check(al != null && al.value().equals(evt.getLabel()), "类 @Label == 类型 label");
        jdk.jfr.Category ac = c.getAnnotation(jdk.jfr.Category.class);
        check(ac != null && java.util.Arrays.equals(ac.value(), evtCats.toArray(new String[0])), "类 @Category == 类型 categories");
        jdk.jfr.Description ad = c.getAnnotation(jdk.jfr.Description.class);
        check(ad != null && "Results of deserialization and ObjectInputFilter checks".equals(ad.value()), "类 @Description == 类型 description");
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

        // ===================== 深化: 录制-回读闭环 (ObjectInputStream 反序列化 ArrayList 触发 jdk.Deserialization; 事件挂在 filterCheck) =====================
        java.nio.file.Path dsDir = java.nio.file.Files.createTempDirectory("evtt");
        try {

            java.io.ByteArrayOutputStream dsBos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream dsOos = new java.io.ObjectOutputStream(dsBos);
            java.util.ArrayList<String> dsList = new java.util.ArrayList<String>();
            dsList.add("a");
            dsList.add("b");
            dsOos.writeObject(dsList);
            dsOos.close();
            jdk.jfr.Recording dsRec = new jdk.jfr.Recording();
            dsRec.enable("jdk.Deserialization");
            dsRec.start();
            java.io.ObjectInputStream dsOis = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(dsBos.toByteArray()));
            java.util.ArrayList<?> dsBack = (java.util.ArrayList<?>) dsOis.readObject();
            dsOis.close();
            dsRec.stop();
            dsRec.dump(dsDir.resolve("dump.jfr"));
            dsRec.close();
            check(dsBack.size() == 2, "反序列化 round-trip 成功");
            int dsCnt = 0;
            jdk.jfr.consumer.RecordedEvent dsHit = null;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(dsDir.resolve("dump.jfr"))) {
                if (!"jdk.Deserialization".equals(e.getEventType().getName())) continue;
                dsCnt++;
                Object t = e.getValue("type");
                if (t instanceof jdk.jfr.consumer.RecordedClass && "java.util.ArrayList".equals(((jdk.jfr.consumer.RecordedClass) t).getName())) dsHit = e;
            }
            check(dsCnt >= 1, "jdk.Deserialization >= 1 条, 实得 " + dsCnt);
            check(dsHit != null, "存在 type == java.util.ArrayList 的事件");
            check(dsHit != null && dsHit.getBoolean("filterConfigured") == false, "未配置 serial filter -> filterConfigured=false");
            check(dsHit != null && dsHit.getValue("filterStatus") == null, "无 filter 判定 -> filterStatus=null");
            check(dsHit != null && dsHit.getInt("arrayLength") == -1, "非数组对象 arrayLength==-1");
            check(dsHit != null && dsHit.getLong("objectReferences") >= 1, "objectReferences >= 1");
            check(dsHit != null && dsHit.getLong("depth") >= 1, "depth >= 1");
            check(dsHit != null && dsHit.getLong("bytesRead") > 0, "bytesRead > 0");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dsDir.resolve("data.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dsDir.resolve("w.bin")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dsDir.resolve("dump.jfr")); } catch (java.io.IOException ignore) { }
            try { java.nio.file.Files.deleteIfExists(dsDir); } catch (java.io.IOException ignore) { }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
