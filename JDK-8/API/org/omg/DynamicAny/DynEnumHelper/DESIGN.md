# org.omg.DynamicAny.DynEnumHelper

## API 功能
DynEnum 对象引用 Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id/type/name/narrow(null)；nil insert → MARSHAL（与 DynAnyHelper 同，区别于 CosNaming objref helper）。
