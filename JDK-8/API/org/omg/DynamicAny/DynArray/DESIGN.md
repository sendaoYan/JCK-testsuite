# org.omg.DynamicAny.DynArray

## API 功能
定长数组的 DynAny 视图：get/set_elements、get/set_elements_as_dyn_any（无 set_length）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 定长 2 元素往返双通道；元素数错 → **InvalidValue**；元素类型错 → TypeMismatch。
