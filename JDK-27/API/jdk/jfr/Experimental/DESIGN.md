# jdk.jfr.Experimental 设计案（JDK-8 侧）

## 1. API 功能
JFR 元数据注解 `@interface Experimental`：`@Retention(RUNTIME)`、`@Target({FIELD,TYPE})`、带`@Inherited`、带`@MetadataDefinition`、不带 `@Documented`/`@Repeatable`。元素：无（标记注解）。

## 2. Java 用户如何使用
标注在自定义事件类/字段上（`static class E extends jdk.jfr.Event`），经 `EventType`/`ValueDescriptor`/`SettingDescriptor`/`AnnotationElement` 与录制-回读链路生效；详见测试内嵌套事件类。

## 3. 参考来源
- OpenJDK 源码：`~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/Experimental.java`（定义处）、`~/git/jdk25u-dev` 同路径（27 侧面）。
- 官方回归：`~/git/jdk/test/jdk/jdk/jfr/api/metadata/annotations/`（注解族契约用例）。
- javadoc：jdk.jfr 包文档（两侧 release）。
- 双侧实测基线：/tmp/jdk-jfr-verified-facts.md + 本批 2026-11-05 探针（本 DESIGN 即实测记录）。

## 4. 测试用例设计 + 易错规范细节
维度：① 元注解契约（Retention/Target 精确集/Documented/Inherited/Repeatable/MetadataDefinition 反射实测）② value 签名与默认值 ③ 常量集精确性 ④ 应用闭环（注解 → EventType/ValueDescriptor 元数据回读 → 录制 → dump → RecordingFile 双通道读回 → 注解与字段值全等）⑤ 负例（先探后断）。

**本注解闭环要点**：类型/字段双面 getAnnotation + annotationElements；录制-回读后仍在；自标签 'Experimental'、自描述含 'not to be shown'。

**本注解负例（实测精确行为）**：未标注事件/字段 getAnnotation==null 且 elements 不含。

### 注解族共享易错点（双侧实测，2026-11-05，jdk8u372-b07 vs jdk-27.0.1.0.2+1）
1. **`getAnnotation(...)` 返回 JFR 代理只支持注解声明成员**：对返回值调 `toString()`/`equals()`/`hashCode()` 抛 `UnsupportedOperationException`（消息 "Flight Recorder proxy only supports members declared in annotation interfaces..."）。测试与消费端必须直接调 `.value()` 等 declared member。
2. **setting descriptor 顺序两侧不同**（8: `enabled, threshold, stackTrace`；27: `threshold, stackTrace, enabled[, throttle]`）——测试一律按名查找，禁止断言顺序。
3. **事件门控规则**：`@Enabled(false)` 事件未被显式 `r.enable(...)` 时 commit 被静默丢弃（不抛异常）；显式 `enable` 或 `.with("enabled","true")` 覆盖注解；`.with("enabled","false")` 覆盖默认 true。`shouldCommit()` 与实际门控一致。
4. **Recording.getSettings() 只含显式设置的键**：注解默认值不出现在 map 中（如 `@Throttle("2/s")` 事件仅 `enable` 后 map 无 throttle 键，需显式 `.with(...)`）。
5. **用户事件不产生 period setting descriptor**；但 27 侧 `@Throttle` 会产生 throttle descriptor（不对称）。jdk.* 周期事件（如 jdk.CPULoad）才有 period descriptor（默认 `everyChunk`）。
6. **@Name 非法值两侧分歧**：8 侧 `""`/含空格/前导点 → 首次解析抛 `ExceptionInInitializerError`（cause=`IllegalArgumentException: "X is not a valid Java identifier"`），且尾点 `"trail."`/连续点 `"a..b"` 被按字面量接受；27 侧一律不抛，打 JVM warning `@Name ignored, not a valid Java type name.` 并回退为 `Class.getName()`。
7. **@Category**：`{}` → `getCategoryNames().size()==0`；`{""}` → **单元素空串列表**（size==1，toString 打印成 `[]` 造成视觉歧义，勿用 toString 判等）；`" "` 字面量保留；缺省 → `[Uncategorized]`。
8. **@Threshold/@Period/@Throttle 非法串**（如 "bogus"）注册期一律容忍：descriptor `getDefaultValue()` 原样保留原始串（27 侧运行时额外打 warning 回退默认值 `0 ns`/`everyChunk`/`off`，8 侧静默）；负值 `-1 s` 同样保留。
9. **@Timespan/@Timestamp/@DataAmount 字段非法值**（如 "bogus"）不校验，`ValueDescriptor.getAnnotation(...).value()` 原样回读。
10. **隐式字段自带注解**：startTime 带 `@Timestamp(TICKS)`、duration 带 `@Timespan(TICKS)`、eventThread/stackTrace 带 Label+Description（文案两侧一致："Thread in which event was committed in" 等）。
11. **@MetadataDefinition 是自定义注解进入 JFR 元数据的开关**：不带它的自定义注解（即使 @Retention(RUNTIME)）在 `getAnnotationElements()` 完全不可见——正负对照实证。
12. **@Registered(false)**：`EventType.getEventType(cls)` 抛 `IllegalStateException("Event class ... is not registered")`；`FlightRecorder.register/unregister` 状态机翻转；@Registered 本身不带 @MetadataDefinition（与多数 jfr 注解不同）。
13. **元注解事实表**：全部 `@Retention(RUNTIME)`、全部**不带** `@Documented`；`@Inherited` 仅在 Category/Enabled/Experimental/Period/Registered/StackTrace/Threshold（+27 的 Throttle）；无 @Repeatable。
14. **@ContentType 承载表**（自带 ContentType 的注解）：BooleanFlag/DataAmount/Frequency/MemoryAddress/Percentage/Timespan/Timestamp/Unsigned；其余（Label/Name/Enabled/...）不带。
15. **@Period 默认值是 `"everyChunk"`**（不是 "0 ns"）；@Threshold 默认 `"0 ns"`；@Enabled/@Registered/@StackTrace 默认 true；@Throttle(27) 默认 `"off"`；@Timespan 默认 NANOSECONDS、常量仅 5 个（无 MINUTES/HOURS/DAYS）；@Timestamp 默认 MILLISECONDS_SINCE_EPOCH；@DataAmount 默认 BYTES。
16. **@Frequency 是标记注解（无 value()）**且 @Target 只有 {FIELD, METHOD}（不能标类型）；@MemoryAddress 同为标记（8/27 一致，无 value）。

