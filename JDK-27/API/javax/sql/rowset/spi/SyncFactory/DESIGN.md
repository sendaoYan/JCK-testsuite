# javax.sql.rowset.spi.SyncFactory 设计案

## 1. API 功能

`SyncFactory` 是 `javax.sql.rowset.spi` 包中的 **同步提供者工厂**（SPI 入口），全部方法为 static，构造函数私有（单例工厂）。核心职责：维护 `SyncProvider` 注册表、按 providerID 返回 `SyncProvider` 实例、管理日志器与 JNDI 上下文。

公开成员（依据 JDK-27 `javap`）：

- 常量：
  - `String ROWSET_SYNC_PROVIDER` = `"rowset.provider.classname"`
  - `String ROWSET_SYNC_VENDOR` = `"rowset.provider.vendor"`
  - `String ROWSET_SYNC_PROVIDER_VERSION` = `"rowset.provider.version"`
- 工厂：
  - `static SyncFactory getSyncFactory()` —— 返回单例
  - `static SyncProvider getInstance(String providerID)` —— 按 ID 取 provider，`null` 抛 `SyncFactoryException`，未知 ID 回退到参考实现 `RIOptimisticProvider`
  - `static Enumeration<SyncProvider> getRegisteredProviders()`
- 注册：
  - `static void registerProvider(String providerID)`
  - `static void unregisterProvider(String providerID)`
- 日志：
  - `static void setLogger(Logger)` / `static void setLogger(Logger, Level)`（`null` 抛 NPE）
  - `static Logger getLogger()`（未设置时抛 `SyncFactoryException`）
- JNDI：
  - `static void setJNDIContext(Context)`（`null` 抛 `SyncFactoryException`）

默认注册的两个参考 provider 来自 `rowset.properties`：`com.sun.rowset.providers.RIOptimisticProvider`、`com.sun.rowset.providers.RIXMLProvider`。

## 2. Java 用户如何使用

```java
// 取单例工厂
SyncFactory f = SyncFactory.getSyncFactory();

// 取参考实现 provider
SyncProvider p = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider");
String id = p.getProviderID();   // com.sun.rowset.providers.RIOptimisticProvider

// 枚举已注册 provider
for (Enumeration<SyncProvider> e = SyncFactory.getRegisteredProviders(); e.hasMoreElements();) {
    e.nextElement().getProviderID();
}

// 注册/注销自定义 provider
SyncFactory.registerProvider("com.example.MyProvider");
SyncFactory.unregisterProvider("com.example.MyProvider");

// 日志
SyncFactory.setLogger(Logger.getLogger("x"));
Logger l = SyncFactory.getLogger();
```

## 3. 开源代码实际用法调研

- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/spi/SyncFactoryTests.java`：
  - `getSyncFactory()` 非 null；`getInstance(propertyStubProvider)` 返回对应 `PropertyStubProvider`；`registerProvider(stubProvider)` 后 `getInstance` 返回 `StubSyncProvider`；
  - `getInstance("")` 与 `getInstance("util.InvalidSyncProvider")` 都返回 `RIOptimisticProvider`（未知/空 ID 回退默认 provider）；
  - `getInstance(null)` 抛 `SyncFactoryException`；未设置日志时 `getLogger()` 抛 `SyncFactoryException`；
  - `setLogger(alogger)` 后 `getLogger()` 返回同一实例；`registerProvider`/`unregisterProvider` 后 `getRegisteredProviders` 集合相应变化；`setJNDIContext(null)` 抛 `SyncFactoryException`。
- JDK 实现 `com/sun/rowset/CachedRowSetImpl.java:365`、`com/sun/rowset/WebRowSetImpl.java:122`：实现内部通过 `SyncFactory.getInstance(DEFAULT_SYNC_PROVIDER)` / `SyncFactory.getInstance(providerName)` 取得 `SyncProvider`。

## 4. 测试用例设计 + 自动化实现

`SyncFactoryTest.java` 分两部分：

- **白盒**：类形态（非接口、public、父类 Object、无接口）、单例、三个常量字符串值。
- **黑盒**：
  - 日志生命周期：未设置时 `getLogger` 抛 `SyncFactoryException` → `setLogger`/`setLogger(logger, level)` 后 `getLogger` 返回同一实例且 level 生效 → `setLogger(null)` 抛 NPE。
  - JNDI：`setJNDIContext(null)` 抛 `SyncFactoryException`。
  - 注册表：枚举含两个默认 provider ID；`getInstance(RIOptimisticProvider)` 的 providerID/vendor/version/grade/lock/view/reader/writer；`getInstance(RIXMLProvider)`。
  - 边界/异常：`getInstance(null)` 抛 SFE；未知 ID 回退 `RIOptimisticProvider`；`registerProvider(bogus)` 后 `getInstance(bogus)` 因类不可加载抛 SFE，`unregisterProvider` 后从注册表消失，未知 ID 的 `unregisterProvider` 为 no-op。

自动化断言总数：36 个 check（白盒 + 黑盒）。
