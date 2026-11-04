# java.util.spi 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

Locale 服务提供者接口（Currency/TimeZone/Locale/CalendarData/CalendarName）+ ResourceBundleControlProvider 匿名实现行为。

## 2. Java 用户如何使用

匿名实现验证方法契约与返回值组合；null 语义属内建 provider（接口层不强制，以直通断言锁定）。

## 3. 参考来源

java.util.spi javadoc。

## 4. 测试用例设计

7 个测试各 testContracts()。
