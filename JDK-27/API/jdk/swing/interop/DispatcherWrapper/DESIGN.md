# jdk.swing.interop.DispatcherWrapper 设计案

## 1. API 功能
Swing 分发器抽象类。

## 2. Java 用户如何使用
嵌入场景子类化。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/DispatcherWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒降级。

## 5. 契约深化（JDK-27）

- 抽象面精确三方法：createSecondaryLoop / isDispatchThread / scheduleDispatch；构造器 public 允许匿名子类。
- 匿名全实现可实例化且行为生效：isDispatchThread 返回覆写值、scheduleDispatch 真实执行传入 Runnable。
- 静态安装点：setFwDispatcher(EventQueue, DispatcherWrapper) 为 static，在全新 EventQueue 上安装不抛异常（headless 安全）。
