# java.beans.PropertyChangeListenerProxy 测试设计案

> 覆盖目标：完整契约（继承 EventListenerProxy、实现 PropertyChangeListener、getPropertyName/getListener、委托）。

## 1. API 功能
- 构造：`PropertyChangeListenerProxy(String propertyName, PropertyChangeListener listener)`。
- 访问：`getPropertyName()`、`getListener()`（继承 EventListenerProxy）。
- 行为：`propertyChange(PropertyChangeEvent)` 委托给被包装监听器。

## 2. 使用方式
- 由 `PropertyChangeSupport` 在命名注册时内部使用；也可手动包装。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyChangeListenerProxy.html`）。
- JCK `api/java_beans/PropertyChangeListenerProxy`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 继承/实现结构 | testStructure |
| P0 | getPropertyName/getListener 身份、propertyChange 委托 | testBasics |
| P1 | null 监听器被接受（getListener 返回 null） | testBasics |

## 关键规范细节
- 构造器**不**对 null 监听器抛异常（EventListenerProxy 只是存储），`getListener()` 返回 null。

---

## 追加覆盖（契约深化，JDK-8 与 JDK-27 行为一致，均为实测）

### 新增测试方法
| 方法 | 覆盖维度 |
|---|---|
| testInventory | 精确公开 API：唯一构造器 (String, PropertyChangeListener)、公开方法恰为 propertyChange+getPropertyName、非 Serializable |
| testNameSemantics | 空串/null/含空格属性名原样保留；getListener 返回**传入的同一对象**；instanceof 三连 |
| testGlobalRegistrationDispatch | **代理解包注册契约**：addPropertyChangeListener(proxy) 被解包为 (name, inner) 命名注册——hasListeners(name)=true；getPropertyChangeListeners() 返回**新**代理包装同一 inner；匹配名转发**同一事件对象**；不匹配/null 名不转发；另一代理（同 inner）可移除；重复 remove no-op |
| testDirectDelegation | 直调无条件委托；null listener → NPE；RuntimeException 同一对象透传 |
| testEqualsHashToString | equals/hashCode 身份语义（非值语义）；toString 非空 |
| testNestedProxy | 嵌套代理直调两级下穿；全局注册只解包一层（以最外层名为键） |
| testNamedRegistration | addPropertyChangeListener("target", proxy)：按注册名存储、触发时无条件委托 |
| testMixedWithPlainListener | 代理+普通监听器/多代理不同名互不串扰 |

### 新增易错细节（实测）
- null listener 构造被接受，但 `propertyChange` 分发时抛 **NPE**（无 null 防护）。
- PCS 对代理的注册/移除是**解包语义**（按 (name, inner) 匹配，与代理对象身份无关）；
  `getPropertyChangeListeners()` 返回的是重新包装的新代理对象（非注册的那个）。
- 转发的事件对象与被 fire 的事件是**同一个**（身份相等）。
- equals/hashCode 未覆写：两个同参代理不相等（身份语义）。
