# SerializationMisdeclarationEvent（JDK-27 侧）—— jdk.jfr.events 契约覆盖设计

## 1. API 功能

`jdk.jfr.events.SerializationMisdeclarationEvent`（jdk.jfr.internal.MirrorEvent 子类）是 JFR 内置事件类，运行时注册为事件类型 **`jdk.SerializationMisdeclaration`**：
- label：`Serialization Misdeclaration`；类目：Java Development Kit > Serialization
- description：Methods and fields misdeclarations. The checks are usually performed just once per serializable class, the first time it is used by serialization. Under high memory pressure, a class might be re-checked again.
- 字段（运行时 ValueDescriptor 顺序）：startTime, misdeclaredClass, message

## 2. 使用方式

未导出包，测试反射 `Class.forName` 读类形与字段；行为面经公共 API `jdk.jfr.FlightRecorder`/`jdk.jfr.Recording`/`jdk.jfr.consumer.RecordingFile` 驱动：`recording.enable("jdk.SerializationMisdeclaration")` → 触发 → `stop()` → `dump(path)` → `RecordingFile.readAllEvents` 回读断言（录制-序列化-回读全链路）。

## 3. 参考来源

- Oracle javadoc：jdk.jfr 包文档与 JDK Mission Control 事件目录（JDK 27）。
- OpenJDK 源码：`~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/events/SerializationMisdeclarationEvent.java`、`jdk/jfr/internal/PlatformRecorder.java`（）。
- 双侧实测（2026-11-05，temurin jdk8u372-b07 / jdk-27.0.1.0.2+1）：事件名册 `FlightRecorder.getEventTypes()`（8=129、27=197 个 jdk.* 类型）、字段表与触发闭环，探测程序 /tmp/probe-d/。

## 4. 测试设计（SerializationMisdeclarationEventTest.java）

1. **结构 shape（既有）**：类加载、final/abstract/public、父类、声明字段等反射断言。
2. **运行时元数据闭环**：名册中定位 `jdk.SerializationMisdeclaration`，断言 getName/getLabel/getCategoryNames/getDescription 精确串、getId>0、isEnabled==false（未录制）、字段名序列逐一精确匹配。
3. **类注解↔运行时元数据一致性**：类上 `@Name/@Label/@Category/@Description` 值与事件类型元数据全等；逐用户字段（含父类）`@Label` 值==ValueDescriptor.getLabel()、类字段类型==vd.getTypeName() 后缀。
4. **版本差异锁定**：27 侧断言 jdk.ObjectAllocationSample/jdk.VirtualThreadPinned 在名册中在册。
5. **不触发说明**：需触发序列化错误声明检查的类形态，属 JVM 内部检查路径。

### 8/27 字段差集（jdk.SerializationMisdeclaration）

该事件类型仅在 JDK-27 名册存在；对侧无对应类型。

### 易错细节

- **类名≠事件名**：ExceptionThrownEvent→`jdk.JavaExceptionThrow`、ErrorThrownEvent→`jdk.JavaErrorThrow`（以类 @Name 为准，勿按类名直推）。
- `Recording.enable(未知名)` **不抛异常**（设置懒惰生效），缺席断言必须走 `getEventTypes()` 名册 contains。
- FileRead/FileWrite/Exception 事件默认有 threshold（20ms 等），精确计数须 `.with("threshold", "0 ns")`。
- 27 侧多数 MirrorEvent 类标 `@RemoveFields({"duration","eventThread","stackTrace"})`，回读时相应字段不存在（如 JavaExceptionThrow 无 duration）。
- 本套件 jtreg 为 othervm 模式，每测试独立 JVM，无跨测试注册干扰。
