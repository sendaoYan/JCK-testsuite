# java.beans.beancontext.BeanContextChildSupport — 设计案（JDK-8 侧）

## 一、API 功能
BeanContextChild 的一般实现基类：implements BeanContextChild, BeanContextServicesListener, Serializable；serialVersionUID 6328947014421475877L。
公共字段 beanContextChildPeer（委托目标）；protected pcSupport/vcSupport（PropertyChangeSupport/VetoableChangeSupport）；transient beanContext/rejectedSetBCOnce。
方法：setBeanContext（veto 协议 + rejectedSetBCOnce 一次性旁路）、getBeanContext、命名 PCL/VCL 注册移除、firePropertyChange/fireVetoableChange、validatePendingSetBeanContext、getBeanContextChildPeer/isDelegated、serviceAvailable/serviceRevoked 空实现、protected releaseBeanContextResources/initializeBeanContextResources 钩子。

## 二、使用方式
直接继承实现 Bean，或以委托方式包装一个已实现 BeanContextChild 的 Bean（peer 构造器，事件 source 为 peer）；属性变化经 firePropertyChange 分发，否决经 fireVetoableChange；上下文 attach/detach 时钩子被回调。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextChildSupport（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextChildSupport.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextChildSupport.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥55，实际 87）：
1. testWhiteBox/testBlackBox：原有结构断言保留（implements 清单、peer、validate 默认 true、fire 方法、service 空实现、peer/delegated 构造器）+ serialVersionUID。
2. testSetBeanContextEvents：fresh null→null 无事件；attach 事件字段逐项（含 source==child）；同值静默；detach old=bc new=null；再附着另一上下文。
3. testVetoPaths：监听器 veto → PVE 且事件携带 (name,old,new) 精确、属性不变、监听器恰被咨询 1 次；rejectedSetBCOnce 旁路：veto 后下一次 set 跳过 veto（监听器 0 次调用）直接生效，成功后标志复位；validatePendingSetBeanContext=false → PVE，同样存在一次性旁路，成功后复位（用非 null 新值验证再拒绝）。
4. testResourceHooks：attach 调 initializeBeanContextResources 一次、detach 调 release 一次、null→null 不调、同值 set 不重复调。
5. testFirePropertyChange：命名选择、事件字段精确、source==child、等值非 null 不发、null old 发、他名不投递、重复注册双发、单 remove 减半、移除陌生监听器无影响。
6. testFireVetoableChange：无监听器不抛；非否决监听器字段精确；等值非 null 不发；他名不投递；双监听器 veto 序列实测 [L1:原事件, L2:原事件, L1:回退事件] 且 PVE 透传（回退事件 newValue==oldValue，L2 在回退阶段的二次异常被吞）；移除后不再收。
7. testPeerDelegation：null peer → self；公共字段与访问器一致；委托构造事件 source==peer；委托 setBeanContext 只改自身不改 peer。
8. testNullRegistrations：addPCL(null,l)/(name,null)、addVCL(null,l)/(name,null)、remove 不存在者均不抛且不投递。
9. testServiceHooks：子类覆写 serviceAvailable/serviceRevoked 被调用。
10. testSerialization：子类状态存活；transient beanContext → null；self peer 保留；可序列化命名监听器存活（经子类暴露 pcSupport.getPropertyChangeListeners(name) 验证副本计数继续增长）；不可序列化委托 peer → IOException（writeObject 显式检查）。
易错细节：① rejectedSetBCOnce 是"一次性旁路"：任何一次 PVE 后下一次 setBeanContext 无条件生效，之后复位——JDK 实测语义（非文档化）；② 事件 source 是 beanContextChildPeer；③ veto 回退事件：非抛出监听器收 2 个事件，抛出监听器只收 1 个；④ 等值非 null 的 firePropertyChange/fireVetoableChange 均不触发。
