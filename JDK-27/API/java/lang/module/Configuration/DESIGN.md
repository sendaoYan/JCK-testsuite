# java.lang.module.Configuration — 设计方案

## 1. API 功能
模块解析结果（final、不可变）：`empty()`（空配置单例）、`resolve/resolveAndBind`（实例版以 this 为父、静态版可传多父）、`parents()`、`modules()`（Set<ResolvedModule>）、`findModule(String)`（Optional）、`toString`（模块名集合）。

## 2. Java 用户如何使用
```java
Configuration parent = ModuleLayer.boot().configuration();
Configuration cf = Configuration.resolve(ModuleFinder.ofSystem(), List.of(parent),
        ModuleFinder.of(), Set.of("java.sql"));
ModuleLayer layer = ModuleLayer.defineModules(cf, clf, definedModules -> ...).layer();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/Configuration.java`：resolve 图算法、服务绑定（resolveAndBind 迭代 provides）。
- JDK `test/jdk/java/lang/module/ConfigurationTest.java`：空根、未知根 FindException、多父合并。
- ModuleLayer.defineModulesWithOneLoader 之前必须先得到 Configuration。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、7 个 public 方法及返回类型。
黑盒：empty() 单例与空集；boot 配置含 java.base、parent 是 empty 配置；实例 resolve(空查找器,空根) 得父为本配置的空图；静态 resolve(java.sql) 拉入 java.xml/java.logging 依赖；resolveAndBind 不小于 resolve；未知根抛 FindException 且消息含模块名。
对应 `ConfigurationTest.java` 的 `testBlackBox()`。
