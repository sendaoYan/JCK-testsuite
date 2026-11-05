# jdk.jfr.AnnotationElement 设计案

## 1. API 功能
JFR 注解元素的运行时表示：构造（注解类+值），getTypeName/getValue/hasValue/getValues。

## 2. Java 用户如何使用
new AnnotationElement(Label.class, "x") 后 getValue("value")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/AnnotationElement.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 构造 + value 回读 + Map 构造。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 构造与读取：typeName=="jdk.jfr.Label"、typeId&gt;0、getValue("value")/hasValue/getValues、getAnnotationElements 空、getAnnotation(Label)==null（不自引用）；Map 构造回读。
- 构造不做值类型校验（实证）：AnnotationElement(Label.class, 5) 构造成功、getValues()==[5]。
- equals 语义（实证）：两个同参新实例不相等（无值 equals，8+27 一致）；equals(self) true；hashCode 调用间稳定。
- EventFactory 回读闭环：Name/Label 元素 → EventType.getAnnotationElements 回读全等，且派生 name/label。
负例精确异常表：
| 操作 | 异常 | 消息 |
|---|---|---|
| new AnnotationElement(null, "x") | NullPointerException | — |
| new AnnotationElement(Name.class)（有 value 成员却单参构造） | IllegalArgumentException | Number of declared methods must match size of value map |
| getValue("missing") | IllegalArgumentException | No value with name 'x'. Valid names are [value] |
另：注解代理只支持声明成员，toString/equals 会 UnsupportedOperationException（规避不测）。
