# javax.management.openmbean.InvalidOpenTypeException — 测试设计

## 1. API 功能
open data 类型非法时抛出的受检异常：继承 IllegalArgumentException。两个构造器：无参（null message）与
String message；getMessage 返回传入 message。

## 2. Java 用户如何使用
CompositeDataSupport 构造时 item 名不在 CompositeType 或值类型不匹配时抛 InvalidOpenTypeException；
用户 catch 它以区分类型非法。链路：构造(非法 item/类型) → throw new InvalidOpenTypeException → getMessage 断言。

## 3. 开源代码实际用法调研
- JDK 自用：CompositeDataSupport/CompositeType/ArrayType 在类型非法时抛 InvalidOpenTypeException
  （`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/InvalidOpenTypeExceptionTest.java` 覆盖构造与 message。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（InvalidOpenTypeExceptionTest.java）
- 白盒：public、extends IllegalArgumentException、两个构造器存在、Serializable。
- 黑盒：无参构造 getMessage==null、带 message 构造 getMessage 精确；instanceof IllegalArgumentException；
  CompositeDataSupport 非法类型构造抛该类型。

## 深化（2026-11，批 G）
- 父类精确 IllegalArgumentException；unchecked；两构造器；SUID=-2837312755412327534L；round-trip。
- 真实触发点：TabularDataSupport.put 行型不符 → "Argument value's composite type […]
  is not assignable to this TabularData instance's row type […]."（两端类型全打印）；被拒行不入表；
  与 KeyAlreadyExistsException 无继承关系；OpenType.isValue 错型返回 false 不抛（对照）。
- 版本差异：零差异。
