# org.omg.DynamicAny.NameDynAnyPair

## API 功能
struct：`FieldName id` + `DynAny value`（get_members_as_dyn_any 通道元素）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- final + IDLEntity；默认 null/null；DynAny 引用原样保存且保持可用（非防御拷贝）；身份 equals。
