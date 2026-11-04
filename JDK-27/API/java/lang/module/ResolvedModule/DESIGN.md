# java.lang.module.ResolvedModule — 设计方案

## 1. API 功能
Configuration 中的已解析模块（final）：`name()`、`configuration()`、`reference()`、`reads()`（Set<ResolvedModule>，同配置内目标）；equals = 同 Configuration + 同名；`toString` = identityHashCode(configuration)/name。

## 2. Java 用户如何使用
```java
for (ResolvedModule rm : configuration.findModule("java.sql").orElseThrow().reads())
    System.out.println(rm.name());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ResolvedModule.java`：`System.identityHashCode(cf) + "/" + name()`。
- JDK `test/jdk/java/lang/module/BasicModuleBuilderTest.java` / `ConfigurationTest.java`：reads 图与相等性。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、7 方法、reads 返回 Set。
黑盒：java.base（name/config/reference、静态图 reads 为空）；java.sql reads java.base/java.xml 且无自读、reads 目标同配置；equals/hashCode（同配置同名相等、null/异类型/跨配置不等、子图同名模块不相等但名字相同）；toString 精确格式 identityHashCode(configuration)/name。
对应 `ResolvedModuleTest.java` 的 `testBlackBox()`。
