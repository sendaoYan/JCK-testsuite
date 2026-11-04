# java.beans.VetoableChangeListenerProxy — 设计案

## 一、API 功能
命名属性过滤器代理：`extends java.util.EventListenerProxy<VetoableChangeListener>`，
`implements VetoableChangeListener`。唯一构造器
`VetoableChangeListenerProxy(String propertyName, VetoableChangeListener listener)`；
公开方法 `getPropertyName()`、`vetoableChange(PropertyChangeEvent) throws PropertyVetoException`。
JDK-8 与 JDK-27 公开 API 完全一致（javap 双侧实证）。

## 二、使用方式
由 `VetoableChangeSupport` 在按名注册时内部使用；也可手动包装。
将代理作为**全局**监听器注册时，VCS 将其解包为 (propertyName, inner) 命名注册——
代理自身即完成按名过滤。

## 三、参考来源
- Oracle javadoc：Java SE 8 API Specification（`java/beans/VetoableChangeListenerProxy.html`）；
  Java SE 27 同名页面（一致）。
- JavaBeans Specification 1.01-A（事件监听器注册模型）。
- OpenJDK `VetoableChangeSupport` 源码中代理解包逻辑（`addVetoableChangeListener` 对
  `VetoableChangeListenerProxy` 的特判）与对应回归思路（仅借鉴思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
| 方法 | 覆盖维度 |
|---|---|
| testLegacyStructure | 保留原白盒断言（非接口/具体类/父类 EventListenerProxy/实现 VetoableChangeListener/两方法） |
| testInventory | 精确公开 API：唯一构造器 (String, VetoableChangeListener)、公开方法恰为 vetoableChange+getPropertyName、非 Serializable、public class |
| testNameSemantics | getPropertyName（普通/空串/null 原样保留）；getListener 返回**传入的同一对象**；null listener 构造被接受 |
| testGlobalRegistrationDispatch | 全局注册解包：hasListeners(name)=true；getVetoableChangeListeners() 返回**新**代理包装同一 inner；匹配名转发**同一事件对象**；不匹配名/null 名不转发；用另一包装同一 inner 的代理 remove 也能移除；重复 remove no-op |
| testVetoThroughProxy | veto 透传：inner 抛 PropertyVetoException → 经 VCS 调用方收到**同一异常对象**；直调代理亦身份保持 |
| testDirectDelegation | 直调无条件委托（不检查名称）；null listener → NPE；RuntimeException 同一对象透传 |
| testEqualsHashToString | equals/hashCode 身份语义（非值语义）；toString 非空 |
| testNestedProxy | 嵌套代理直调两级下穿；全局注册只解包一层（以最外层名为键） |
| testNamedRegistration | 命名注册 addVetoableChangeListener("target", proxy)：按注册名存储、触发时无条件委托、不注册代理自身名称 |
| testMixedWithPlainListener | 代理+普通监听器/多代理不同名互不串扰 |

### 易错规范细节
- null listener 构造被接受（EventListenerProxy 仅存储），但 `vetoableChange` 分发时抛 NPE
  （JDK-8/JDK-27 均实测）。
- VCS 对代理的注册/移除是**解包语义**：按 (propertyName, inner) 匹配，与代理对象身份无关；
  `getVetoableChangeListeners()` 返回的是重新包装的新代理对象。
- veto 异常经代理与 VCS 两层都以**同一对象**透传（身份不被包装/拷贝）。
- equals/hashCode 未被覆写：身份语义，两个同参代理不相等。
