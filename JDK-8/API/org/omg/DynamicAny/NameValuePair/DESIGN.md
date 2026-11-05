# org.omg.DynamicAny.NameValuePair

## API 功能
struct：`FieldName id` + `any value`（DynStruct 成员表元素）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- final + IDLEntity；默认 null/null；Any 引用**原样保存**；equals/hashCode 未覆写（身份语义）。
