# java.util.prefs 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面（双侧行为一致，共同锁定）。

## 2. Java 用户如何使用

同一套隔离与清理约定。

## 3. 参考来源

同 JDK-8。

## 4. 测试用例设计

与 JDK-8 同名 testContracts。
