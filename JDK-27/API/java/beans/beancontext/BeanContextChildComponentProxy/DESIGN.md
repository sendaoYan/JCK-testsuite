# java.beans.beancontext.BeanContextChildComponentProxy — 设计案

## 1. API 功能
接口。`Component getComponent()` 返回子 Bean 关联的 AWT 组件。

## 2. Java 用户如何使用
实现该接口的 Bean 暴露其可视组件，供 BeanContext 容器布局。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextChildComponentProxy.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现返回 JLabel 的 getComponent。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`（原文件以 @key headful 换取 AWT 组件构造；实测 **JLabel 在 headless 下可构造**，无需 headful）。追加深度（8→36 断言）：单方法精确清单、返回型恰 Component、无父接口、与相邻代理接口独立、非 @FunctionalInterface；实现契约——组件身份稳定（每调同实例）、null 返回合法、多实现多态分派计数、**Label 在 headless 抛 HeadlessException 而 Panel 可构造**（以 Panel 做纯 AWT 例）、实现异常原样传播；反射分派（Method.invoke 到具体/null 实现、声明类为接口）；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
