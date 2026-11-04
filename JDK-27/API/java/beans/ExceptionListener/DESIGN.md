# java.beans.ExceptionListener — 设计案

## 1. API 功能
异常监听器接口，单一方法 `exceptionThrown(Exception)`；XMLDecoder/XMLEncoder 用其报告编解码异常。

## 2. Java 用户如何使用
`xmlDecoder.setExceptionListener(e -> ...)` 捕获解析异常而不中断。

## 3. 开源代码实际用法调研
JDK 源码 `ExceptionListener.java`；`XMLDecoder`/`XMLEncoder`/`Encoder` 异常报告接口。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现验证 exceptionThrown 分发。
