# StackFilter（JDK-27 侧）—— jdk.jfr.events 契约覆盖设计

## 1. API 功能

`jdk.jfr.events.StackFilter` 是**事件注解**（@interface，JDK 22 引入，包内镜像）：`String[] value()` 指定要从栈迹中剔除的类/方法（`com.example.Logger::log` 或整类名；不支持通配符）。元注解 `@Target({TYPE}) @Inherited @Retention(RUNTIME) @MetadataDefinition`。注意：**公共 API 并无 jdk.jfr.StackFilter**（内部注解从未导出，实测 Class.forName 抛 CNFE）；栈迹相关公共注解是 `jdk.jfr.StackTrace`。

## 2. 使用方式

未导出包（`jdk.jfr.events` 不在 `jdk.jfr` 模块导出清单），测试一律反射 `Class.forName` 读取类/注解/字段；注解的运行时消费方是 JFR 元数据系统（`MetadataRepository`）。

## 3. 参考来源

- Oracle javadoc：jdk.jfr 包文档与 JDK Mission Control 事件目录（JDK 27）。
- OpenJDK 源码：`~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/events/StackFilter.java`、`jdk/jfr/internal/PlatformRecorder.java`（）。
- 双侧实测（2026-11-05，temurin jdk8u372-b07 / jdk-27.0.1.0.2+1）：事件名册 `FlightRecorder.getEventTypes()`（8=129、27=197 个 jdk.* 类型）、字段表与触发闭环，探测程序 /tmp/probe-d/。

## 4. 测试设计（StackFilterTest.java）

1. **结构 shape（既有）**：类加载、final/abstract/public、父类、声明字段等反射断言。
2. **注解契约深化**：元注解面（Target/Inherited/Retention RUNTIME/MetadataDefinition 或 Relational/Label）、成员面（value() 返回型与默认值，或无成员）。
3. **使用点闭环**：FileReadEvent/SocketReadEvent 类携带 @jdk.jfr.events.StackFilter；断言公共 API 无 jdk.jfr.StackFilter（CNFE）而 jdk.jfr.StackTrace 存在。

### 8/27 字段差集（N/A）

注解类型：仅 27 侧存在（8 无 jdk.jfr.events.StackFilter/CertificateId）。

### 易错细节

- **类名≠事件名**：ExceptionThrownEvent→`jdk.JavaExceptionThrow`、ErrorThrownEvent→`jdk.JavaErrorThrow`（以类 @Name 为准，勿按类名直推）。
- `Recording.enable(未知名)` **不抛异常**（设置懒惰生效），缺席断言必须走 `getEventTypes()` 名册 contains。
- FileRead/FileWrite/Exception 事件默认有 threshold（20ms 等），精确计数须 `.with("threshold", "0 ns")`。
- 27 侧多数 MirrorEvent 类标 `@RemoveFields({"duration","eventThread","stackTrace"})`，回读时相应字段不存在（如 JavaExceptionThrow 无 duration）。
- 本套件 jtreg 为 othervm 模式，每测试独立 JVM，无跨测试注册干扰。
