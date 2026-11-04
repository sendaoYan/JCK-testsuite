# java.lang.module.ModuleDescriptor.Builder — 设计方案

## 1. API 功能
final 嵌套 Builder（流式）：
- `requires(...)` 5 重载（含 Requires 对象）、`exports/opens` 各 5/4 重载（含对象）、`provides`（service+providers 或对象）、`uses`、`packages`、`version(String/Version)`、`mainClass`
- 终结：`build()` 产出不可变 `ModuleDescriptor`

## 2. Java 用户如何使用
```java
ModuleDescriptor md = ModuleDescriptor.newModule("m")
        .requires(Set.of(TRANSITIVE), "java.sql")
        .exports("m.api", Set.of("m.client"))
        .provides("m.S", List.of("m.Impl"))
        .mainClass("m.Main").version("1.0").build();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：Builder 对重复声明抛 ISE、automatic 模块禁 requires/exports/opens、open 模块禁 opens。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：全部重载与异常路径。
- jdeps/jlink 内部（`jdk.internal.module.Builder` 派生）构造系统模块描述符。

## 4. 测试用例设计 + 自动化实现
白盒：final、嵌套于 ModuleDescriptor、`build()` 返回类型、8 个流式方法存在。
黑盒：
- 全链式调用返回同一 builder（this）+ build 后各集合 size/内容回读；
- `version(String)`/`version(Version)`、`requires(Requires)`/`exports(Exports)`/`opens(Opens)`/`provides(Provides)` 对象重载往返；
- 重复 requires/exports 抛 ISE；open 模块声明 opens 抛 ISE；非法包名 IAE。
对应 `BuilderTest.java` 的 `testBlackBox()`。
