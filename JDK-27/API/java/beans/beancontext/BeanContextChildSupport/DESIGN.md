# java.beans.beancontext.BeanContextChildSupport — 设计案

## 1. API 功能
BeanContextChild 便利基类（implements BeanContextChild, BeanContextServicesListener, Serializable）。构造器 `()`/`(peer)`；set/getBeanContext、add/removePropertyChangeListener、add/removeVetoableChangeListener、firePropertyChange/fireVetoableChange、validatePendingSetBeanContext、getBeanContextChildPeer/isDelegated、serviceRevoked/serviceAvailable。

## 2. Java 用户如何使用
继承之实现 BeanContextChild；peer 构造器实现委托。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextChildSupport.java`；beancontext 实现基类。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanContextChild + Serializable。黑盒（`testBlackBox`）：beanContext set/get、peer 委托、fire 系列、service 回调、validatePending。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 13 → 87。新增（与 JDK-8 侧同源）：
  - testSetBeanContextEvents：fresh null→null 无事件、attach 事件字段逐项（name/old/new/source）、同值静默、detach、再附着。
  - testVetoPaths：监听器 veto → PVE（PVE 事件字段精确、属性不变、咨询 1 次）；**rejectedSetBCOnce 一次性旁路**（veto 后下一次 set 跳过 veto 直接生效，成功后复位）；validatePendingSetBeanContext=false → PVE + 同样的旁路与复位。
  - testResourceHooks：initialize/releaseBeanContextResources 恰在非 null 转换时各调一次。
  - testFirePropertyChange：命名选择、等值非 null 不发、null old 发、重复注册双发、单 remove 减半。
  - testFireVetoableChange：无监听器不抛、字段精确、veto 序列实测 [L1:原, L2:原, L1:回退(newValue==oldValue)]、PVE 透传。
  - testPeerDelegation：null peer → self、公共字段、事件 source==peer、setBeanContext 只改自身。
  - testNullRegistrations / testServiceHooks。
  - testSerialization：子类状态存活、transient beanContext → null、可序列化命名监听器存活并继续触发、不可序列化委托 peer → IOException。
- JDK-27 版本差异：整类 @Deprecated(forRemoval=true) since 23；行为与 JDK-8 全部一致。
