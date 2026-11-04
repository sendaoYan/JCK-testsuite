# java.util.logging.Logger 测试方案

白盒结构断言在 `LoggerTest.testWhiteBox()`，黑盒行为断言在 `LoggerTest.testBlackBox()`。

## 1. API 功能

- **工厂**：`getLogger(String)`、`getLogger(String,String)`、`getGlobal()`、`getAnonymousLogger()`、`getAnonymousLogger(String)`。
- **日志方法**：`log(Level,...)`（String/Object/Object[]/Throwable/Supplier）、`logp`、`logrb`、`entering/exiting/throwing`、`severe/warning/info/config/fine/finer/finest`。
- **配置**：`setLevel/getLevel`（null 表示继承）、`setFilter/getFilter`、`addHandler/removeHandler/getHandlers`、`setUseParentHandlers/getUseParentHandlers`、`setResourceBundle/getResourceBundle/getResourceBundleName`、`getParent`、`isLoggable`、`getName`。

## 2. Java 用户如何使用

- 具名层级：`Logger.getLogger("com.app.Module")`；父子自动建立。
- 关闭父级传播：`logger.setUseParentHandlers(false); logger.addHandler(customHandler);`
- 等级过滤：`logger.setLevel(Level.WARNING); if (logger.isLoggable(Level.INFO)) ...`
- 参数化：`logger.log(Level.INFO, "value={0}", v); logger.log(Level.INFO, () -> expensive());`

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/Logger.java:680`：`public static Logger getLogger(String name)`。
- `test/jdk/java/util/logging/MemoryHandlerTest.java:53`：`Logger.getLogger(MemoryHandlerTest.class.getName())`。
- `test/jdk/java/util/logging/ClassLoaderLeakTest.java:183`：`Logger.getLogger("app_test_logger")`。
- `test/jdk/java/util/logging/TestLogConfigurationDeadLockWithConf.java:219,276`：`Logger.getLogger("foo.bar"+...)` 与 `Logger.getLogger("remaining")`。
- `src/java.desktop/share/classes/java/awt/EventQueue.java:181`：`PlatformLogger.getLogger("java.awt.event.EventQueue")` —— JDK 自身用同款命名风格。

## 4. 测试用例设计 + 自动化实现

- **层级**：`getLogger("jck.logging.h.A.B").getParent() == getLogger("jck.logging.h.A")`。
- **等级**：get/set、null 继承、isLoggable 三态。
- **组合**：捕获 Handler + `useParentHandlers(false)`；7 个便捷方法等级正确；`log(Level,String/Object/Object[]/Throwable/Supplier)`；`entering/exiting` 产生 FINER ENTRY/RETURN。
- **全局/匿名**：`getGlobal()` 名为 "global"；`getAnonymousLogger()` 名为 null 且有父。
- **健壮性**：具名 logger + useParentHandlers(false)，无控制台输出、无全局污染。
