# org.omg.DynamicAny.DynValueBox

## API 功能
boxed valuetype 的 DynAny 视图（get/set_boxed_value 双通道）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 直接接口精确序：DynValueBoxOperations, DynValueCommon, IDLEntity；是 DynAny 但**不是** DynValue（兄弟分支）。
- 离线无法构造 valuetype TypeCode（ORB 无 create_value_tc），行为闭环按设计跳过。
