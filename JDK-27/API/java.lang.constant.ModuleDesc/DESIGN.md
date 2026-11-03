# java.lang.constant.ModuleDesc — 设计方案

## 1. API 功能
模块名义描述（interface）：`of(String)`、`name()`、值相等 equals。

## 2. Java 用户如何使用
```java
ModuleDesc m = ModuleDesc.of("java.base");
if (m.name().equals(layerObject.descriptor().name())) ...
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/ModuleDesc.java`；实现为 `ModuleDescImpl`（仅包名记录）。
- ClassFile API（JDK 24+ ClassFile.Module）用其表示 requires 目标。

## 4. 测试用例设计 + 自动化实现
白盒：接口、of 静态、name 抽象返回 String。
黑盒：of/name；值相等 equals/hashCode（跨实例相等、null/异类型不等）；空串名保留；of(null) NPE；HashSet 按值去重。
对应 `ModuleDescTest.java` 的 `testBlackBox()`。
