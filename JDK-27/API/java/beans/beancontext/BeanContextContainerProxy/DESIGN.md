# java.beans.beancontext.BeanContextContainerProxy — 设计案

## 1. API 功能
接口。`Container getContainer()` 返回 BeanContext 关联的 AWT 容器。

## 2. Java 用户如何使用
实现该接口的 BeanContext 暴露其容器供子组件挂载。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextContainerProxy.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现返回 Panel 的 getContainer。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`（原文件构造 java.awt.Panel；实测 Panel 在 headless 下可构造，无需 headful）。追加深度（8→36 断言）：单方法精确清单、返回型恰 Container、无父接口、与相邻代理接口独立、非 @FunctionalInterface；实现契约——容器身份稳定、null 返回合法、多实现多态分派计数、普通 AWT 嵌套（add/remove + getComponentCount）与代理契约解耦、实现异常原样传播；反射分派（Method.invoke 到具体/null 实现、声明类为接口）；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
