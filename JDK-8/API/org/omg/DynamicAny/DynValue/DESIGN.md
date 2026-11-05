# org.omg.DynamicAny.DynValue

## API 功能
valuetype 的 DynAny 视图（成员访问同 DynStruct 模型）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 直接接口**精确序**：DynValueOperations, DynValueCommon, IDLEntity（经 DynValueCommon 传递为 DynAny）。
- ORB 无 create_value_tc（TypeCodeFactory 专属）→ 离线无法构造 valuetype TypeCode，行为闭环按设计跳过并记录原因。
