# jdk.jfr.AnnotationElement 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
注解元素：以类形式表达事件/字段注解（如 AnnotationElement(Label.class, "L")），供 EventFactory 使用。

## 2. Java 用户如何使用
new AnnotationElement(Label.class, "L") 或 Map 构造；EventFactory.create 注解列表传入。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 AnnotationElementTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.AnnotationElement.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
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
