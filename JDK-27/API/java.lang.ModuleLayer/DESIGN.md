# ModuleLayer — 设计方案

## 1. API 功能
`java.lang.ModuleLayer`（final）表示模块层（一组已解析模块的运行时视图）：
- 静态：`boot()`、`empty()`
- 查询：`configuration/modules/parents/findModule/findLoader`
- 构建：`defineModulesWithOneLoader/WithManyLoaders/defineModules`

## 2. Java 用户如何使用
- `ModuleLayer.boot()` 获取启动层，`findModule(name)` 查模块，`findLoader(name)` 查类加载器。
```java
ModuleLayer.boot().findModule("java.base").ifPresent(m -> ...);
```

## 3. 开源代码实际用法调研
- 插件/容器用 `defineModules*` 动态构建模块层；JDK `test/jdk/java/lang/ModuleLayer` 系列验证 boot/empty/构建。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ModuleLayer")` 验证 final、非接口。
黑盒：boot 层含 `java.base`、模块/配置非空、`findLoader("java.base")` 为 null、恰有一个父层（empty 层）；empty 层无模块。
对应 `<Simple>Test.java` 黑盒部分。
