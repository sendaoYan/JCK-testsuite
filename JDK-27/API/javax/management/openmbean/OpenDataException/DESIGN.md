# javax.management.openmbean.OpenDataException — 测试设计

## 1. API 功能
open data 相关失败时抛出的受检异常：继承 JMException（因此受检）。两个构造器：无参（null message）与
String message；getMessage 返回传入 message。

## 2. Java 用户如何使用
CompositeType/TabularType 构造校验（重复 item 名、非法 index 名等）抛 OpenDataException；用户 catch 以处理
open data 结构错误。链路：构造(非法结构) → throw new OpenDataException → getMessage 断言。

## 3. 开源代码实际用法调研
- JDK 自用：CompositeType/TabularType/ArrayType 结构校验失败时抛 OpenDataException
  （`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenDataExceptionTest.java` 覆盖构造与 message。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenDataExceptionTest.java）
- 白盒：public、extends JMException（受检）、两个构造器存在、Serializable。
- 黑盒：无参构造 getMessage==null、带 message 构造 getMessage 精确；instanceof Exception/JMException；
  CompositeType 重复 item 名构造抛该类型。
