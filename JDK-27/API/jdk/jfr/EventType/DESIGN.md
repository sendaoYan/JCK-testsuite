# jdk.jfr.EventType 设计案

## 1. API 功能
事件类型元数据：getEventType(Class)/getName/getLabel/getFields/getField/getSettingDescriptors。

## 2. Java 用户如何使用
EventType.getEventType(MyEvent.class).getField("msg")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventType.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 自定义事件类型 name/label/fields/settings。
