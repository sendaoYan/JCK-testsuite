# org.omg.DynamicAny.DynSequence

## API 功能
sequence 的 DynAny 视图：get/set_length、get/set_elements、get/set_elements_as_dyn_any。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 无界序列长度 0→2→0 生命周期；elements 双通道（Any[] 与 DynAny[]）值往返。
- 负例三态（实测精确类型）：set_length(-1) → **NegativeArraySizeException**（JDK 未按规范抛 InvalidValue，锁实测）；set_elements(null) → NPE；元素类型错 → TypeMismatch。
