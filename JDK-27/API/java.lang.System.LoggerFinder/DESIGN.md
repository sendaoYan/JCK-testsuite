# System.LoggerFinder — 设计方案

## 1. API 功能
`java.lang.System.LoggerFinder`（abstract 服务提供者基类，构造器 protected）：抽象 `getLogger(String, Module)`；具体 `getLocalizedLogger(String, ResourceBundle, Module)` 默认包装 `getLogger` 结果；静态 `getLoggerFinder()` 返回已安装的提供者（JDK 默认为 JUL 桥 `sun.util.logging.internal.LoggingProviderImpl`）。

## 2. Java 用户如何使用
- 库/框架实现自己的日志后端并经 ServiceLoader 安装；业务代码无感地用 `System.getLogger(name)`。
```java
public class MyFinder extends System.LoggerFinder {
    public System.Logger getLogger(String name, Module module) { return new MyLogger(name); }
}
```

## 3. 开源代码实际用法调研
- JEP 264（统一日志 API）；JDK `test/jdk/java/lang/System/LoggerFinder` 大量 SPI/桥接测试；Tomcat/Hibernate 的 `System.Logger` 适配即实现该抽象类。

## 4. 测试用例设计 + 自动化实现
白盒：验证 abstract、构造器 protected、`getLogger` public abstract、`getLocalizedLogger` 具体、`getLoggerFinder` public static。
黑盒：自定义 `RecordingFinder` 子类（protected 构造器可继承）记录 name/module 并返回可写日志；`getLocalizedLogger` 默认委托 `getLogger`（名字一致、日志可写不抛）；默认提供者创建的 logger 名称正确、多种 log 重载（含参数/异常）不抛。
对应 `LoggerFinderTest.java` 白盒 + 黑盒部分。
