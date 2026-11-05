# org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper

## API 功能
TypeMismatch 异常 Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id/type 精确 + Any/CDR 双通道往返（reason 丢失属正常线上语义）。
