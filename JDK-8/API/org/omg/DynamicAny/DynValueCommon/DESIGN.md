# org.omg.DynamicAny.DynValueCommon

## API 功能
可空 valuetype 的公共视图（is_null/set_to_null/set_to_value）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 直接接口精确序：DynValueCommonOperations, DynAny, IDLEntity——它是 DynValue 与 DynValueBox 两分支的**公共基**。
