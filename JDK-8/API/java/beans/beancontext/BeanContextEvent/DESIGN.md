# java.beans.beancontext.BeanContextEvent — 设计案（JDK-8 侧）

## 一、API 功能
抽象基类：extends EventObject；唯一 protected 构造器 (BeanContext)；方法 getBeanContext/getPropagatedFrom/setPropagatedFrom/isPropagated（后三者 synchronized）；protected 字段 propagatedFrom（@serial，非 transient）。isPropagated() 实现为 propagatedFrom != null（设为 source 自身也算 propagated）。serialVersionUID 7267998073569045052L。

## 二、使用方式
所有 beancontext 事件的基类；setPropagatedFrom 标记事件自嵌套层级中的哪个上下文传播而来。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextEvent（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextEvent.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextEvent.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥35，实际 46）：
1. testStructure：抽象、superclass EventObject、4 方法声明、唯一 protected (BeanContext) 构造器、propagation 访问器 synchronized、protected propagatedFrom 字段类型 BeanContext、serialVersionUID、newInstance → InstantiationException。
2. testBlackBox（原断言保留）：getBeanContext/getSource 身份、默认 null/未传播、set/生效。
3. testPropagationSemantics：set 为 source 自身 → isPropagated true（非 null 判定规则）；set(null) 允许并复位；last-wins；getBeanContext/getSource 不受传播影响；toString 不抛。
4. testNullSource：子类 null source → IAE（EventObject 契约，两种子类）。
5. testSubeventHierarchy：Membership/ServiceAvailable/ServiceRevoked 三个具体子类 assignable；membership 子类继承传播机制；service 事件基类型引用 getBeanContext 正确。
6. testSerialization（经具体子类）：source transient → getSource/getBeanContext null；propagatedFrom 存活为副本；未传播往返后仍未传播；子类负载存活。
易错细节：① isPropagated 恰为 (propagatedFrom != null)，实测 source 自身也返回 true，与 javadoc 自然语言有出入；② null source 抛 IAE（EventObject），不是 NPE；③ 子类化是使用该类的唯一途径（abstract）。
