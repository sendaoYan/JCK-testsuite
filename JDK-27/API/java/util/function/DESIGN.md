# java.util.function 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面 + Predicate.not（JDK-11+）。

## 2. Java 用户如何使用

not(String::isEmpty) 组合；其余真值表/组合矩阵与 JDK-8 一致（双侧锁定一致性）。

## 3. 参考来源

JDK-11 Predicate.not javadoc。

## 4. 测试用例设计

JDK-8 拷贝 + Predicate.not 行为断言与反射签名锁定。
