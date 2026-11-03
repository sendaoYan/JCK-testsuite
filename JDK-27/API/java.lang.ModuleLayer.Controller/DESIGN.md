# ModuleLayer.Controller — 设计方案

## 1. API 功能
`java.lang.ModuleLayer.Controller`（final，无公开构造器）控制一个 `ModuleLayer`：`layer()` 返回受控层；`addReads(Module, Module)`、`addExports(Module, String, Module)`、`addOpens(Module, String, Module)`、`enableNativeAccess(Module)` 修改层内模块的可达性/原生访问并返回 this（链式）。

## 2. Java 用户如何使用
- 通过 `ModuleLayer.defineModules*` 创建层时拿到 Controller，事后调整层内模块的 reads/exports/opens（容器、插件框架、应用服务器）。
```java
Controller c = ModuleLayer.defineModulesWithOneLoader(cf, parents, loader);
c.layer(); c.addReads(m1, m2);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/ModuleLayer` 用 Controller 验证 addReads/addExports 语义；OSGi/准容器框架以它实现模块间后期接线。

## 4. 测试用例设计 + 自动化实现
白盒：验证 final、无公开构造器、`layer()` 返回 `ModuleLayer`、四个控制方法均返回 `Controller`（链式）。
黑盒：以 `Configuration.empty()` + 空父列表构建空层，验证 `layer()` 的模块/父层/配置一致；对层外模块（java.base）调用 `addReads/enableNativeAccess` 断言抛 `IllegalArgumentException`。
对应 `ControllerTest.java` 白盒 + 黑盒部分。
