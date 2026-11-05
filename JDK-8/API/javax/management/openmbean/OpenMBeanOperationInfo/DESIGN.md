# javax.management.openmbean.OpenMBeanOperationInfo — 测试设计

## 1. API 功能
开放 MBean 操作元数据接口。方法集 9 个：getName、getDescription、getSignature、getReturnOpenType、
getImpact、equals、hashCode、toString（getImpact/getSignature 经 MBeanOperationInfo 继承面暴露）。
impact 常量由 javax.management.MBeanOperationInfo 提供：INFO=0/ACTION=1/ACTION_INFO=2/UNKNOWN=3。

## 2. Java 用户典型用法
new OpenMBeanOperationInfoSupport(name, desc, sig, returnOpenType, impact) 放入 OpenMBeanInfoSupport；
客户端据 getReturnOpenType 渲染返回值、据 getImpact 决定按钮文案。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenMBeanOperationInfo。
- OpenJDK 源码同名接口 + MBeanOperationInfo（impact 常量权威）。
- 双侧 javap 实测。

## 4. 测试用例设计 + 易错细节（OpenMBeanOperationInfoTest，批 G 深化）
- 结构：精确方法集 9 个；无公开字段；extends MBeanOperationInfo（类实现关系在 Support 侧）。
- 契约：getReturnOpenType 经接口引用等于构造传入的 SimpleType 单例；SimpleType.VOID 表示无返回；
  impact 四常量值经接口引用可读且等于 MBeanOperationInfo 常量；equals 不含 description、
  含 signature 内容与返回类型与 impact。
- 版本差异：两侧一致。
