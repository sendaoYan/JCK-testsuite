# java.beans.PropertyChangeListener — 设计案

## 1. API 功能
属性变更监听器接口（extends EventListener），单一方法 `propertyChange(PropertyChangeEvent)`。

## 2. Java 用户如何使用
实现 `propertyChange` 接收属性变更；`PropertyChangeSupport.addPropertyChangeListener` 注册。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyChangeListener.java`；JavaBeans/Swing 事件模型核心接口。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现验证 propertyChange 分发。

---

## 追加覆盖（契约深化，与 JDK-8 侧同源；JDK-8 与 JDK-27 行为一致，均为实测）

新增 testInventory / testNameSemantics / testGlobalRegistrationDispatch / testDirectDelegation /
testEqualsHashToString / testNestedProxy / testNamedRegistration / testMixedWithPlainListener。

关键契约（实测）：
- addPropertyChangeListener(proxy) 全局注册被解包为 (name, inner) 命名注册；
  getPropertyChangeListeners() 返回**新**代理包装同一 inner。
- 匹配名转发**同一事件对象**；不匹配名/null 名不转发；null listener 分发时 NPE。
- equals/hashCode 身份语义；toString 非空；非 Serializable。
- 嵌套代理全局注册只解包一层（以最外层名为键）。
