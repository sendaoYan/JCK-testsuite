# java.beans.PropertyChangeEvent 测试设计案

> 覆盖目标：完整契约（构造器 + null source→IAE、四个 getter、propagationId、toString）。

## 1. API 功能
- 构造：`PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue)`。
- 访问：`getSource`/`getPropertyName`/`getOldValue`/`getNewValue`；`setPropagationId`/`getPropagationId`；`toString`。

## 2. 使用方式
- 由 `PropertyChangeSupport` 构造并投递给监听器；监听器读取 old/new 值判断变化。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyChangeEvent.html`）。
- OpenJDK `test/jdk/java/beans/PropertyChangeEvent`；JCK `api/java_beans/PropertyChangeEvent`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 + source 身份 + 四个 getter | testConstructor |
| P0 | null source→IllegalArgumentException；null name/old/new 允许 | testConstructor |
| P1 | propagationId 默认 null、set/get、set null | testPropagationId |
| P1 | toString 含 name/old/new/propagationId/source | testToString |

## 关键规范细节
- 构造器委托 `EventObject(source)`，null source 抛 `IllegalArgumentException`。
- toString 形如 `PropertyChangeEvent[propertyName=p; oldValue=old; newValue=new; propagationId=...; source=...]`。
