# java.beans.PropertyVetoException 测试设计案

> 覆盖目标：完整契约（继承 Exception、message、getPropertyChangeEvent 身份、null 事件、throw/catch）。

## 1. API 功能
- 构造：`PropertyVetoException(String mess, PropertyChangeEvent evt)`。
- 访问：`getPropertyChangeEvent()`。

## 2. 使用方式
- 约束属性 setter 或 `VetoableChangeSupport` 监听器抛出，阻止赋值。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyVetoException.html`）。
- JCK `api/java_beans/PropertyVetoException`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | extends Exception、message、getPropertyChangeEvent 身份 | testBasics |
| P0 | null message / null event 保留 | testBasics |
| P1 | throw/catch 往返 | testThrowCatch |

## 关键规范细节
- `getPropertyChangeEvent()` 返回构造时传入的同一对象（含 null）。
- 该异常是受检异常（extends Exception），调用方必须捕获或声明。
