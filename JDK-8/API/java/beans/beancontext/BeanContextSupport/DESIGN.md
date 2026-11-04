# java.beans.beancontext.BeanContextSupport — 设计案（JDK-8 侧）

## 一、API 功能
BeanContext 的默认实现基类：extends BeanContextChildSupport implements BeanContext, Serializable, PropertyChangeListener, VetoableChangeListener。
构造器 5 个：(peer, locale, dTime, visible)/(peer, locale, dTime)/(peer, locale)/(peer)/()，locale 为 null 时取 Locale.getDefault()。
成员契约：add/remove/size/isEmpty/contains/containsKey/containsAll/iterator/toArray()/toArray(T[])；addAll/removeAll/retainAll/clear 无条件抛 UnsupportedOperationException。
成员事件：add/removeBeanContextMembershipListener（null 入参 NPE）；add 成功 fire childrenAdded（source=peer）、remove 成功 fire childrenRemoved。
资源委托：getResourceAsStream/getResource —— name 或 child 为 null 抛 NPE，非成员 child 抛 IAE("Not a valid child")，成员按 child 的 ClassLoader 解析。
本地标志：setDesignTime/isDesignTime（变更时 fire "designMode" 属性变更 old=!new）；setLocale/getLocale（null 无操作、同值无事件、变更走 veto+change 双事件）。
GUI 可见性：needsGui（Visibility 子项或 Visibility peer 委托）/avoidingGui(!okToUseGui && needsGui)/dontUseGui/okToUseGui（向子 Visibility 传播）。
子协议：add 时对 BeanContextChild 子项调 setBeanContext(peer)（veto → IllegalStateException 且不入册）；remove 时通知 setBeanContext(null)；子项自行 setBeanContext(null) → 上下文静默移除它。
instantiateChild → Beans.instantiate(classloader, name, peer) 并把新 Bean 加入自身。
序列化：serialVersionUID -4879613978649577204L；非 Serializable 子项写时静默丢弃；bcmListeners 为 transient。

## 二、使用方式
作为 Bean 容器直接实例化或继承；add/remove 维护子 Bean 集合；成员监听器感知增删；子 Bean 实现 BeanContextChild 即被自动接线（加入即 setBeanContext、移除即置 null）；getResource* 以子 Bean 的类加载器解析资源；嵌套子上下文形成层级（globalHierarchyLock 串行化）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextSupport（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextSupport.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextSupport.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥70，实际 149）：
1. testWhiteBox：类层级/implements 清单/nested classes 恰为 {BCSChild, BCSIterator}/serialVersionUID 稳定。
2. testMembershipContract：add true、重复 add false 且不发事件、remove absent false、contains/containsKey/containsAll（子集/全集/超集/空集）。
3. testNegativeMatrix（负例矩阵，双侧实测一致）：add(null)→IllegalArgumentException；remove(null)→IllegalArgumentException；add/removeBeanContextMembershipListener(null)→NullPointerException；getResourceAsStream(null,member)/(name,null)/(name,非成员)→NPE/NPE/IAE；getResource 同构；成员资源 "java/lang/String.class" 命中（URL 协议两侧不同 jrt/jar，只断言非 null）。
4. testToArrayContract：toArray() 精确元素；toArray(T[]) 精确长度/复用传入数组（==）/大数组 null 终结符。
5. testMembershipEventDetails：事件携带子项身份（==）；事件 source==bc；重复注册监听器只发一次；移除后不再收；两个监听器都收到；监听器 RuntimeException 从 add() 透传且子项已加入、后续监听器被跳过。
6. testIteratorContract：内容集合相等（不断言顺序）、迭代不修改、两次 iterator() 独立、hasNext 幂等、空上下文 false。
7. testUnsupportedBulkOps：addAll/removeAll/retainAll/clear 均 UOE 且子项不变。
8. testLocaleAndDesignTime：默认 runtime + 默认 Locale；"designMode" 事件 old/new 精确（Boolean.FALSE→TRUE）、同值不发；"locale" 同值/null 不发、变更 veto+change、veto 抛 PropertyVetoException 且 locale 不变；构造器组合。
9. testGuiVisibility：空上下文 needsGui/avoidingGui false；Visibility 子项联动；dontUseGui/okToUseGui 向子传播；needsGui 恒 true 的子项 + dontUseGui → avoidingGui true（= !okToUseGui && needsGui）。
10. testChildProtocol：BCCS 子项加入后 getBeanContext()==bc；bc.remove 通知子项 old=bc new=null；子项自行 setBeanContext(null) → 自动移除 + childrenRemoved（身份）；veto 子项 add → IllegalStateException + 未入册 + 无事件；validatePendingAdd=false 同果；嵌套 BCS：inner.getBeanContext()==outer。
11. testPeerSemantics：peer 构造器 peer 身份；事件 source==peer；needsGui 委托 Visibility peer；默认 self peer。
12. testSerializationContract：非序列化子项丢弃（size 3→2）；designTime/locale/peer self 存活；已接线子项以副本存活且被重新嵌套（getBeanContext()!=null）；bcmListeners 不存活；peer 不可序列化 → NotSerializableException；peer 可序列化 → 委托关系存活。
13. testInstantiateChild：实例化并入册 + 事件；no.such.BeanXYZ→ClassNotFoundException；null→NullPointerException。
14. testChangeHandlerHooks：无关属性/无 pending-remove 的 beanContext 事件不扰动成员。
易错细节：① toArray(T[]) 复用传入数组（返回 ==）；② contains 是 equals（HashMap keySet），contains(null) false 不抛；③ add(null)/remove(null) 是 IAE 不是 NPE；④ 事件 source 是 peer 不是 BCS 自身；⑤ needsGui 遍历时第一个 Visibility 子项即决定返回值；⑥ 序列化时 BeanContextChildSupport 子项的 transient beanContext 由 BCS 读回时重新接线。
