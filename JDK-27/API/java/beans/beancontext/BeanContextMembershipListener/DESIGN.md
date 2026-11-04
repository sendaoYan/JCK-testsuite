# java.beans.beancontext.BeanContextMembershipListener — 设计案

## 1. API 功能
成员监听器接口（extends EventListener）。childrenAdded/childrenRemoved。

## 2. Java 用户如何使用
注册到 BeanContext 接收子 Bean 增删事件。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextMembershipListener.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现 childrenAdded/childrenRemoved 分发。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 4 → 40。新增：恰 2 方法（排序相等）、参数类型/void/abstract 声明；真实 BeanContextSupport 流程驱动（add → childrenAdded 事件字段全断言含子项身份 ==、remove → childrenRemoved、重复注册幂等、移除后静默、双监听器、失败操作不发事件——重复 add false/absent remove false/veto 子项 IllegalStateException/add(null) IAE）；监听器 RuntimeException 从 add()/remove() 透传且状态已变更、后续监听器被跳过。
- JDK-27 与 JDK-8 完全一致。
