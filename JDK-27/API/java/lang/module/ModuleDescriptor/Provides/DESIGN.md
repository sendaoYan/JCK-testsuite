# java.lang.module.ModuleDescriptor.Provides — 设计方案

## 1. API 功能
服务提供项（final 值对象）：`service()`（服务类名）、`providers()`（实现类名 List，保序）；`compareTo`（按 service 名）；equals 含 providers 列表与顺序。

## 2. Java 用户如何使用
```java
newModule("m").provides("m.Spi", List.of("m.ImplA", "m.ImplB")).build();
descriptor.provides().forEach(p -> p.providers())...
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`。
- ServiceLoader 与 ModuleReader 组合按 provides 发现服务（java.base provides java.nio.charset.spi.CharsetProvider 等）。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、service/providers 返回类型。
黑盒：service/providers 保序回读；equals/hashCode/compareTo（含顺序与数量参与相等）；toString 内容；真实模块 java.base 的 provides 非空。
对应 `ProvidesTest.java` 的 `testBlackBox()`。
