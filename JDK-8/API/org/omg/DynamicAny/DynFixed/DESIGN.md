# org.omg.DynamicAny.DynFixed

## API 功能
fixed 定点数的 DynAny 视图（get_value/set_value 字符串通道）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- get_primitive_tc(tk_fixed) 可得 fixed(0,0) TypeCode；但离线 create_dyn_any_from_type_code(tk_fixed) → **BAD_OPERATION**（SUN 213，
  默认 fixed Any 无法构造）——行为覆盖按实测记录为跳过原因，仅保留形态断言（方法面 get_value/set_value + 继承）。
