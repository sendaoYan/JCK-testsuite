# java.beans.beancontext.BeanContextEvent — 设计案

## 1. API 功能
BeanContext 事件抽象基类（extends EventObject，protected 构造器）。getBeanContext/getPropagatedFrom/setPropagatedFrom/isPropagated。

## 2. Java 用户如何使用
所有 beancontext 事件的基类；isPropagated 表示事件从嵌套上下文传播而来。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends EventObject + abstract。黑盒（`testBlackBox`）：子类 getBeanContext、propagatedFrom 默认/set/isPropagated。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 8 → 46。新增：唯一 protected (BeanContext) 构造器、propagation 访问器 synchronized、protected propagatedFrom 字段、serialVersionUID 7267998073569045052L、newInstance → InstantiationException；**isPropagated 恰为 (propagatedFrom != null)**——set 为 source 自身也算 propagated（实测，与 javadoc 自然语言有出入）；set(null) 允许并复位；last-wins；null source → IAE（EventObject 契约，两种具体子类验证）；三个具体子类 assignable + 基类型引用行为；序列化（source transient、propagatedFrom 副本存活、未传播保持）。
- JDK-27 与 JDK-8 完全一致（类自 JDK-23 起 deprecated for removal）。
