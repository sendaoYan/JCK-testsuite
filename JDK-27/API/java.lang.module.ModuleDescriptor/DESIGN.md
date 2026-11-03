# java.lang.module.ModuleDescriptor — 设计方案

## 1. API 功能
模块描述符（final、只读、无 public 构造器）：
- 工厂：`newModule(name[, modifiers])`、`newOpenModule(name)`、`newAutomaticModule(name)`、`read(InputStream/ByteBuffer[, packageFinder])`
- 查询：`name/isOpen/isAutomatic/requires/exports/opens/uses/provides/version/rawVersion/toNameAndVersion/mainClass/packages/modifiers/accessFlags`
- 比较：`compareTo`（按名）、`equals/hashCode`（按名）

## 2. Java 用户如何使用
```java
ModuleDescriptor d = ModuleDescriptor.newModule("com.app")
        .requires("java.sql").exports("com.app.api")
        .provides("com.app.Spi", List.of("com.app.Impl")).build();
d.name(); d.requires(); ... // 读回全部
ModuleDescriptor info = Module.getDescriptor(); // 运行时模块
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：Builder 校验（automatic 模块禁 requires/exports/opens）与隐式 java.base MANDATED require。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：Builder 全 API + read 回读 + 异常路径。
- jlink/jdeps、OSGi Equinox 的模块适配层都消费 ModuleDescriptor API。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、4 个静态工厂存在。
黑盒：
- `read(InputStream)` 读 java.base 真实 module-info（requires 为空、导出 java.lang、packages、无 mainClass）；
- 全功能 Builder 描述符逐 getter 回读（qualified exports/opens targets、STATIC requires、uses、provides 顺序、version/mainClass/toNameAndVersion）；
- 隐式 java.base MANDATED require；open/automatic 变体（automatic 声明 requires 抛 ISE）；
- `version(String)` 重载；equals/hashCode/compareTo 按名；`read(ByteBuffer)` 与 InputStream 等价；null 名 NPE。
对应 `ModuleDescriptorTest.java` 的 `testBlackBox()`。
