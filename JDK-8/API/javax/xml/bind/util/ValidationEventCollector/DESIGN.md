# javax.xml.bind.util.ValidationEventCollector 契约覆盖设计（JDK-8）

## 1. API 功能
开箱即用的事件收集器（implements ValidationEventHandler）：`handleEvent` 记录事件并**返回 true**（容忍一切、让操作继续），`getEvents()` 拷贝导出、`hasEvents()`/`reset()` 管理生命周期。常用于诊断：装上后跑完 unmarshal 再检查收集到什么。

## 2. 使用方式
```java
ValidationEventCollector c = new ValidationEventCollector();
u.setEventHandler(c);
u.unmarshal(source);
if (c.hasEvents()) for (ValidationEvent e : c.getEvents()) ...
c.reset();
```
真实通道（本测试实证）：好 XML → 0 事件；意外元素 XML → 恰 1 个 ERROR 事件（locator line 1）且 unmarshal 照常完成。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/util/ValidationEventCollector.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/util/ValidationEventCollector.java`
- 本仓库测试：`ValidationEventCollectorTest.java`

## 4. 测试用例设计 + 易错规范细节
- 初始态：hasEvents false、getEvents() **非 null 空数组**。
- 记录生命周期：handleEvent 返回 true（锁定）；事件同实例入列、message/severity/locator 可回读；两事件按插入序返回；**getEvents() 每次返回新数组**（两次调用 != 同一实例，锁定）；reset 清空且实例可复用。
- 负例（实测锁定，RI 怪癖）：`handleEvent(null)` → **NullPointerException，但 null 已先入列**（hasEvents true、getEvents()[0]==null）；reset 可清除。
- 真实通道：装到真实 Unmarshaller 上——好数据 0 事件；`<extra>` 意外元素恰 1 个 ERROR 事件（message 含 "unexpected element"、locator line==1）且 round-trip 完成（true 裁决）；reset 后复用。
- 易错点：断言"null 不被记录"会失败——RI 先 add 后解引用；真实通道下 collector 的 true 裁决让坏数据完成是设计行为不是 bug。
