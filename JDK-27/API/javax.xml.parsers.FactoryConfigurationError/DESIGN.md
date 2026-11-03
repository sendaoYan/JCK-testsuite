# javax.xml.parsers.FactoryConfigurationError — 设计案

## 1. API 功能
Error，解析器工厂无法创建/配置时抛出。构造器 `FactoryConfigurationError()/ (String)/ (Exception)/ (Exception, String)`；`getMessage()`、`getException()`（原始异常）、`getCause()`；继承自 `Error`。

## 2. Java 用户如何使用
```java
catch (FactoryConfigurationError e) { e.getException(); }
```
组合链路：构造 → `getMessage/getException/getCause` 关联。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp` 与 `FactoryFinder` 内部：工厂加载失败时 `throw new FactoryConfigurationError(cause, message)`。
- 本仓库既有白盒断言覆盖四种构造器与 `Error` 继承。

## 4. 测试用例设计 + 自动化实现
黑盒（`FactoryConfigurationErrorTest.testBlackBox()`）：`new(msg)` message 精确；`new(Exception)` 的 `getException==getCause==cause` 且 message 由异常派生；`new(Exception, msg)` message 优先、exception 保留；`new()` 全 null；`instanceof Error`。
