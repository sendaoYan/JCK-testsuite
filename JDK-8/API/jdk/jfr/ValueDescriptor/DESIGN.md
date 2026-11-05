# jdk.jfr.ValueDescriptor 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
值描述符：事件字段的类型+名字+注解元数据（EventFactory 字段表构件）。

## 2. Java 用户如何使用
new ValueDescriptor(String.class, "n") 或带注解列表；EventFactory.create fields 传入。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 ValueDescriptorTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.ValueDescriptor.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 基础：name/typeName（String→java.lang.String、int→"int"、long→"long"）、无注解时 label/description/contentType 均 null、isArray false、typeId&gt;0、String 字段无子字段、getAnnotation(Label) null。
- 带注解：Label/Description 元素 → label/description/注解代理全等回读、annotationElements==2。
- Thread 结构类型：版本差异+顺序敏感——27 侧先初始化 JFR 元数据后 getFields()==6；8 侧恒 0；27 侧在元数据初始化前创建 Thread 描述符会毒化类型库（后续初始化 InternalError），测试必须先 EventType.getEventType(某事件) 再建 Thread 描述符。
- EventFactory 回读：字段 label/typeName/注解代理回读；隐式 startTime/duration/eventThread/stackTrace 也是 ValueDescriptor。
负例精确异常表：
| 操作 | 异常 | 消息 |
|---|---|---|
| new ValueDescriptor(String.class, null) | NullPointerException | 8 无消息 / 27 "name" |
| new ValueDescriptor(null, "n") | NullPointerException | 8 无消息 / 27 "type" |
| new ValueDescriptor(Object.class, "n") | IllegalArgumentException | Only primitive types, java.lang.Thread, java.lang.String and java.lang.Class are allowed |
| new ValueDescriptor(String.class, "") | 版本差异：8 不抛 / 27 IAE "is not a valid Java identifier" | |
| new ValueDescriptor(String.class, "n", null) | NullPointerException（null 注解列表，双侧） | |
