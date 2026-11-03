# java.util.EventObject — 测试设计案

## 1. API 功能
所有事件对象的根类：携带事件源，`implements Serializable`。

- 构造：`EventObject(Object source)`（source 非 null，否则 IllegalArgumentException）。
- 查询：`Object getSource()`。
- 其他：`toString()`（`类名[source=...]`）。

## 2. Java 用户如何使用
`class XxxEvent extends EventObject { XxxEvent(Object src){super(src);} }`；事件消费者读
`event.getSource()` 定位来源。
链路：new EventObject(src) -> getSource -> toString -> 子类继承。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/EventObject.java`（本地 `~/git/jdk25u-dev`）：
  构造 null 抛 IllegalArgumentException；toString 拼 `类名[source=..]`——第 2/3 节对齐。
- OpenJDK `src/java.desktop/share/classes/java/beans/PropertyChangeEvent.java` 等继承
  EventObject。
- Spring 的 `ApplicationEvent` 语义同源（事件基类携带 source）。

## 4. 测试用例设计 + 自动化实现（EventObjectTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| getSource | 返回构造参数（身份） |
| null 源 | 抛 IllegalArgumentException |
| toString | 含类名与 source |
| 子类 | 继承 getSource、toString 用子类名 |
| 独立性 | 同源事件对象互异 |

共 5 组场景、约 8 个 check（`testBlackBox()`）。
