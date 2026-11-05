# org.omg.DynamicAny.DynEnum

## API 功能
IDL 枚举的 DynAny 视图：get/set_as_string 与 get/set_as_ulong 双视图。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 默认值=首成员 A/ulong 0；两个 setter 双视图同步；未知成员名/越界 ulong/空串 → **DynAnyPackage.InvalidValue**；
  count 0、type/Any tk_enum、equal(copy)/异成员分化。
