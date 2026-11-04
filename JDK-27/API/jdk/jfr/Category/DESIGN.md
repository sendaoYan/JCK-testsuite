# jdk.jfr.Category 设计案

## 1. API 功能
JFR 元数据注解（`@interface Category`，`@Retention(RUNTIME)`）。元素：`value()`（类型 String[]）。

## 2. Java 用户如何使用
标注在事件类/字段/方法上，供 JFR 解析元数据：`@Category({"a","b"}) static class MyEvent extends Event {}`，运行时通过 `MyEvent.class.getAnnotation(Category.class).value()` 读取。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Category.java`（定义处）。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/jfr/` 中大量事件类用该注解标注名称/标签/单位等元数据。

## 4. 测试用例设计 + 自动化实现
白盒：public/interface/annotation/继承 Annotation/无父类/单接口/@Retention(RUNTIME)/@Target 存在/value 元素类型。黑盒：应用到静态嵌套类后 `getAnnotation(...).value()` 回读一致。
