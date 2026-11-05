# ErrorThrownEvent（JDK-8 侧）—— jdk.jfr.events 契约覆盖设计

## 1. API 功能

`jdk.jfr.events.ErrorThrownEvent`（jdk.jfr.events.AbstractJDKEvent 子类）是 JFR 内置事件类，运行时注册为事件类型 **`jdk.JavaErrorThrow`**：
- label：`Java Error`；类目：Java Application
- description：An object derived from java.lang.Error has been created. OutOfMemoryErrors are ignored
- 字段（运行时 ValueDescriptor 顺序）：startTime, duration, eventThread, stackTrace, message, thrownClass

## 2. 使用方式

未导出包，测试反射 `Class.forName` 读类形与字段；行为面经公共 API `jdk.jfr.FlightRecorder`/`jdk.jfr.Recording`/`jdk.jfr.consumer.RecordingFile` 驱动：`recording.enable("jdk.JavaErrorThrow")` → 触发 → `stop()` → `dump(path)` → `RecordingFile.readAllEvents` 回读断言（录制-序列化-回读全链路）。

## 3. 参考来源

- Oracle javadoc：jdk.jfr 包文档与 JDK Mission Control 事件目录（JDK 8u262+ backport）。
- OpenJDK 源码：`~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/events/ErrorThrownEvent.java`、`jdk/jfr/internal/PlatformRecorder.java`（8u 同构 backport）。
- 双侧实测（2026-11-05，temurin jdk8u372-b07 / jdk-27.0.1.0.2+1）：事件名册 `FlightRecorder.getEventTypes()`（8=129、27=197 个 jdk.* 类型）、字段表与触发闭环，探测程序 /tmp/probe-d/。

## 4. 测试设计（ErrorThrownEventTest.java）

1. **结构 shape（既有）**：类加载、final/abstract/public、父类、声明字段等反射断言。
2. **运行时元数据闭环**：名册中定位 `jdk.JavaErrorThrow`，断言 getName/getLabel/getCategoryNames/getDescription 精确串、getId>0、isEnabled==false（未录制）、字段名序列逐一精确匹配。
3. **类注解↔运行时元数据一致性**：类上 `@Name/@Label/@Category/@Description` 值与事件类型元数据全等；逐用户字段（含父类）`@Label` 值==ValueDescriptor.getLabel()、类字段类型==vd.getTypeName() 后缀。
4. **版本差异锁定**：8 侧断言 5 个 27 独有事件名（ObjectAllocationSample/VirtualThreadStart/X509Certificate/TLSHandshake/ContainerCPUUsage）在名册中缺席。
5. **录制-回读闭环**：抛带唯一 message 的 LinkageError：过滤恰 1 条 `jdk.JavaErrorThrow`，thrownClass 回读 `java.lang.LinkageError`。同名坑同上。

### 8/27 字段差集（jdk.JavaErrorThrow）

8 侧多: duration；27 侧多: 无（27 侧类注解 @RemoveFields({"duration"}) 等删除隐式字段）

### 易错细节

- **类名≠事件名**：ExceptionThrownEvent→`jdk.JavaExceptionThrow`、ErrorThrownEvent→`jdk.JavaErrorThrow`（以类 @Name 为准，勿按类名直推）。
- `Recording.enable(未知名)` **不抛异常**（设置懒惰生效），缺席断言必须走 `getEventTypes()` 名册 contains。
- FileRead/FileWrite/Exception 事件默认有 threshold（20ms 等），精确计数须 `.with("threshold", "0 ns")`。
- 27 侧多数 MirrorEvent 类标 `@RemoveFields({"duration","eventThread","stackTrace"})`，回读时相应字段不存在（如 JavaExceptionThrow 无 duration）。
- 本套件 jtreg 为 othervm 模式，每测试独立 JVM，无跨测试注册干扰。
