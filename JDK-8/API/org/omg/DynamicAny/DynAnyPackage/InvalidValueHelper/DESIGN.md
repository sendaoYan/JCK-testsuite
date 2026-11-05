# org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper

## API 功能
InvalidValue 异常 Helper（id/type/insert/extract/read/write）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确；type tk_except/0 成员；insert 后 Any.type().equal(helper.type())；Any+CDR 双通道往返。
