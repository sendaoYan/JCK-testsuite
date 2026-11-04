# java.lang.module.ModuleDescriptor.Opens — 设计方案

## 1. API 功能
开放包项（final 值对象）：`source()`、`targets()`、`isQualified()`、`modifiers()`（SYNTHETIC/MANDATED）、`accessFlags()`、`compareTo`（按 source）。语义：开放反射深访问。

## 2. Java 用户如何使用
```java
newModule("m").opens("p.impl", Set.of("spring.core")).build();
descriptor.opens().stream().filter(Opens::isQualified)...
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：open 模块本身无逐包 opens。
- Spring Framework 溡 Hamcrest 等依赖 `--add-opens`；库内通过 Module.isOpen/addOpens 组合本类型。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、9 方法存在。
黑盒：qualified/unqualified 回读与相等；按 source compareTo；targets 参与相等；open 模块 opens() 为空（整模块开放）。
对应 `OpensTest.java` 的 `testBlackBox()`。
