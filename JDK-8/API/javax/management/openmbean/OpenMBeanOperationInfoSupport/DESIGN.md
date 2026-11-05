# javax.management.openmbean.OpenMBeanOperationInfoSupport — 测试设计

## 1. API 功能
OpenMBeanOperationInfo 实现（extends MBeanOperationInfo）。构造 (name,desc,sig,returnOpenType,
impact[,Descriptor])；getReturnOpenType() 返回开放类型；getSignature() 每次返回新数组但元素共享
（浅拷贝）；equals = name+sig+returnOpenType+impact（**不含 description**；signature 用
Arrays.equals 顺序敏感）。

## 2. Java 用户典型用法
声明开放操作与返回开放类型；SimpleType.VOID 表示无返回值；impact 用 MBeanOperationInfo 常量。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码（数组拷贝与 equals 实现）；双侧 javap。

## 4. 测试用例设计 + 易错细节（OpenMBeanOperationInfoSupportTest，批 G：64→79 断言）
- getReturnOpenType 单例；VOID 返回；impact 0..3 常量值锁定。
- getSignature 独立性：两次调用不同引用、内容相等；构造拷贝入参数组、元素共享。
- equals：desc 不参与、signature 顺序敏感（交换顺序不等）、impact 参与与 MBeanOperationInfo
  同名字段一致性。
- 序列化 SUID 锁定。
- 版本差异：两侧零差异。
