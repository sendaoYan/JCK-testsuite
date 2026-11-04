# java.util.stream 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

惰性求值（peek 计数短路）、encounter order（并行 forEachOrdered/findFirst）、Collectors 全家族（toMap 重复键 ISE、groupingBy 矩阵）、Collector 特性位、StreamSupport 特性透传、Builder 状态机。

## 2. Java 用户如何使用

limit 约束上游处理次数；sorted 稳定；of(null 数组) NPE vs of(null 标量) 单元素对照；onClose 双抛异常 suppressed。

## 3. 参考来源

java.util.stream javadoc 与 package-summary；OpenJDK 流测试。

## 4. 测试用例设计

Stream/IntStream/LongStream/DoubleStream/BaseStream/Collector/Collectors/StreamSupport/Builder×4 各 testContracts。
