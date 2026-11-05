# java.applet.Applet.AccessibleApplet 测试设计（JDK-8 侧）

## 1. API 功能
Applet 的无障碍上下文：protected 非静态内部类，继承 `Panel$AccessibleAWTPanel`，
单一构造器接收外层 Applet。

## 2. Java 用户如何使用
```java
AccessibleContext ac = applet.getAccessibleContext();  // 即 AccessibleApplet 实例
ac.getAccessibleRole();      // AccessibleRole.FRAME（applet 以框架角色暴露）
ac.getAccessibleStateSet();  // 含 ENABLED
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `java/applet/Applet.java` 内嵌 AccessibleApplet（role=FRAME 的来源）。
- 辅助技术经 `AccessibleContext` 链访问；无独立开源直接用户（容器协议消费方）。
## 4. 测试用例设计 + 自动化实现
- 白盒：protected 修饰、非静态内部类、超类精确 `java.awt.Panel$AccessibleAWTPanel`、
  单构造器 `(Applet)`、可达 AccessibleContext 链。
- 黑盒：getAccessibleContext 非 null 且类型名精确 `java.applet.Applet$AccessibleApplet`；
  role==FRAME；stateSet 含 ENABLED；默认 accessible name null；children 计数 0；
  未挂父组件时 indexInParent==-1、accessibleParent null；locale 未 displayable →
  IllegalComponentStateException 分支；两个 applet 的 context 相互独立。
- 需 headful（@key headful）。
- 实现见 `AccessibleAppletTest.java`。
