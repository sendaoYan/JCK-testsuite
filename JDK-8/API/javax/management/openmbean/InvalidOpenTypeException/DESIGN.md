# javax.management.openmbean.InvalidOpenTypeException — 测试设计

## 1. API 功能
unchecked 异常（直接 extends IllegalArgumentException）。构造链 ()/(String)；
SUID=-2837312755412327534L。

## 2. Java 用户典型用法
对已有开放数据/类型做类型断言失败时抛出：TabularDataSupport.put/calculateIndex 收到与 row type
不匹配的行、OpenType.isValue 族校验路径。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码 TabularDataSupport.checkCompositeType/calculateIndex。

## 4. 测试用例设计 + 易错细节（InvalidOpenTypeExceptionTest，批 G）
- 层级（父类精确 IAE）/unchecked/两构造器数/SUID/round-trip。
- 真实触发点：TabularDataSupport.put 行型不符 → 消息形如
  "Argument value's composite type [CompositeType(name=B,…)] is not assignable to this TabularData
  instance's row type [CompositeType(name=A,…)]."（含两端类型全打印，endPeriod 结尾）；被拒行不入表。
- OpenType.isValue 对错型直接返回 false（不抛）——对照断言。
- 与 KeyAlreadyExistsException 无继承关系。
- 版本差异：两侧零差异。
