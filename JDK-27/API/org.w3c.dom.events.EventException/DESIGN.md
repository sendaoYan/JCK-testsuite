# org.w3c.dom.events.EventException — 设计案

## 1. API 功能
org.w3c.dom.events.EventException 类。常量：UNSPECIFIED_EVENT_TYPE_ERR（共 1 个）。

## 2. Java 用户如何使用
常量断言 + new(short,String) → code/getMessage

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.EventException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventExceptionTest.testBlackBox()`）覆盖：常量 + code 字段 + 构造。
