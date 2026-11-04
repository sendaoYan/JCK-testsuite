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
