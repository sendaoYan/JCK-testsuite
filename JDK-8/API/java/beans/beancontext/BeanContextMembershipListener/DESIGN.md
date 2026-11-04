# java.beans.beancontext.BeanContextMembershipListener — 设计案（JDK-8 侧）

## 一、API 功能
接口：extends java.util.EventListener；声明恰 2 个方法 childrenAdded(BeanContextMembershipEvent)/childrenRemoved(BeanContextMembershipEvent)，均 void、均 abstract（JDK 未给 default 实现）。

## 二、使用方式
向 BeanContext.addBeanContextMembershipListener 注册，感知子 Bean 增删；与 BeanContextSupport 真实流程联动验证。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextMembershipListener（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextMembershipListener.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextMembershipListener.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 40）：
1. testStructure：接口、EventListener assignable、恰 2 方法（排序相等）、参数类型/返回 void/abstract。
2. testDelivery（真实 BeanContextSupport 流程）：add → childrenAdded 恰一次，事件 getBeanContext==bc、getSource==bc、size 1、携带原子项（==）、未传播；remove → childrenRemoved 同构；重复注册幂等（只发一次）；移除监听后静默；两监听器都收到；失败操作不发事件（重复 add false、absent remove false、veto 子项 add 的 IllegalStateException、add(null) IAE）。
3. testExceptionPropagation：childrenAdded 抛 RuntimeException → 从 add() 透传（子项已加入、后续监听器跳过）；childrenRemoved 抛 → 从 remove() 透传（子项已移除）。
易错细节：① 异常发生在子项已变更之后（fire 是 add/remove 的最后一步）；② 监听器列表有 contains 去重，同一监听器不会双发（与 PCS 的可重复注册相反）。
