# java.lang.constant.PackageDesc — 设计方案

## 1. API 功能
包名义描述（interface）：`of(二进制名)`、`ofInternalName(内部名)`；`internalName()`（抽象）、`name()`（default，由 internalName 推导）；值相等。

## 2. Java 用户如何使用
```java
PackageDesc p = PackageDesc.of("java.lang");
p.internalName(); // "java/lang"
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/PackageDesc.java`。
- ClassFile API（ClassFile.CodeBuilder/模块属性）以 internalName 形式消费。

## 4. 测试用例设计 + 自动化实现
白盒：接口、5 方法、of/ofInternalName 静态、name 为 default。
黑盒：dotted/slashed 双向映射与互换等价；根包双空串；值相等（含跨工厂形式 HashSet 去重）；of("java/lang") IAE、分号 IAE、两个工厂 null NPE。
对应 `PackageDescTest.java` 的 `testBlackBox()`。
