# java.beans.beancontext.BeanContextSupport — 设计案

## 1. API 功能
BeanContext 实现基类（implements BeanContext, Serializable）。构造器 `(peer, locale, designTime, okToUseGui)` 系列；size/isEmpty/contains/containsKey/add/remove/addAll/removeAll/retainAll/clear/iterator/toArray；add/removeBeanContextMembershipListener、getResource/getResourceAsStream、instantiateChild、setDesignTime/isDesignTime、setLocale/getLocale、needsGui/dontUseGui/okToUseGui/avoidingGui、isSerializing、getBeanContextPeer。

## 2. Java 用户如何使用
直接实例化或继承作为 Bean 容器；add 添加子 Bean，成员监听器接收增删事件。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java`；beancontext 包核心实现。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanContext + Serializable。黑盒（`testBlackBox`）：集合操作、designTime/locale、gui 可见性、instantiateChild、成员事件触发、构造器。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**：本目录测试纯 headless（不实例化任何 AWT 组件，java.beans.beancontext 仅在签名中引用 AWT 类型），保留该标记会导致 headless CI 漏跑，已移除并在 jtreg `-verbose:summary` 下验证通过。
- 断言数 33 → 176。新增测试方法（与 JDK-8 侧同源，内容一致）：
  - testMembershipContract：add/重复 add（false 且不发事件）/remove absent/containsAll 矩阵。
  - testNegativeMatrix：add(null)→IAE、remove(null)→IAE、null 监听器→NPE、getResourceAsStream/getResource 的 null/非成员→NPE/IAE（成员命中 JDK 资源，jrt 协议只断言非 null）。
  - testToArrayContract：toArray(T[]) 复用传入数组（==）、大数组 null 终结符。
  - testMembershipEventDetails：childrenAdded/Removed 携带子项身份（==）、source==bc、重复注册监听器幂等、移除后静默、双监听器、监听器 RuntimeException 从 add() 透传且后续监听器跳过。
  - testIteratorContract / testUnsupportedBulkOps（addAll/removeAll/retainAll/clear → UOE）。
  - testLocaleAndDesignTime："designMode"/"locale" 事件 old/new 精确、同值/null 不发、locale veto → PVE 且不变、构造器组合（null locale 回退默认）。
  - testGuiVisibility：Visibility 子项联动、dontUseGui/okToUseGui 传播、needsGui 恒 true 子项 + dontUseGui → avoidingGui true。
  - testChildProtocol：BCCS 子项接线（getBeanContext==bc）、bc.remove 通知 old=bc new=null、子项自行 detach → 自动移除 + 事件、veto 子项 add → IllegalStateException 未入册无事件、validatePendingAdd=false 同果、嵌套上下文。
  - testPeerSemantics：事件 source==peer、needsGui 委托 Visibility peer。
  - testSerializationContract：非序列化子项丢弃（3→2）、已接线子项副本重新嵌套、bcmListeners transient 丢失、不可序列化 peer → NotSerializableException、可序列化 peer 委托存活。
  - testInstantiateChild：入册 + 事件、no.such.BeanXYZ → CNFE、null → NPE。
  - testChangeHandlerHooks：无关 PCE/VCE 不扰动成员。
- JDK-27 版本差异：BeanContextSupport 整类自 JDK-23 起 @Deprecated(since=23, forRemoval=true)（javap 实证），测试未加 @SuppressWarnings 时 javac 仅告警不报错；行为与 JDK-8 全部一致（探针对比）。serialVersionUID 双侧一致 -4879613978649577204L。
