# javax.xml.parsers.ParserConfigurationException — 设计案

## 1. API 功能
受检异常，解析器工厂配置非法时抛出。构造器 `ParserConfigurationException()` / `ParserConfigurationException(String message)`；继承自 `Exception`。

## 2. Java 用户如何使用
```java
try { DocumentBuilderFactory.newInstance().newDocumentBuilder(); }
catch (ParserConfigurationException e) { e.getMessage(); }
```
组合链路：构造 → `getMessage`/`getCause`/`toString`。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp`：直接 `new ParserConfigurationException(msg)` 作为 `ErrorHandler.fatalError` 的受检异常包装。
- 本仓库既有白盒断言覆盖构造器与继承关系。

## 4. 测试用例设计 + 自动化实现
黑盒（`ParserConfigurationExceptionTest.testBlackBox()`）：`new(msg)` message 精确 + cause 默认 null；`new()` message null；`toString` 前缀；`instanceof Exception`。
