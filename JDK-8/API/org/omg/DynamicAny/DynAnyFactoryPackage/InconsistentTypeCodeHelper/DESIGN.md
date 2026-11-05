# org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper

## API 功能
InconsistentTypeCode 异常 Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确（DynAnyFactory 包路径嵌在 repoId 中）+ type tk_except + Any/CDR 双通道。
