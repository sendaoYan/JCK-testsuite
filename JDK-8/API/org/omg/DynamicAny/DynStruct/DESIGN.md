# org.omg.DynamicAny.DynStruct

## API 功能
struct 的 DynAny 视图：get/set_members（NameValuePair[]）、get/set_members_as_dyn_any、current_member_name/kind、seek 遍历。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 成员表长度/id/kind 精确；seek(1) 后 current_member_name 联动；当前成员 insert/get_string。
- set/get_members 值往返（long 77 + string sv）；NameDynAnyPair 通道双向；component_count==2。
- JDK-8 宽容行为记录：set_members(null)/空数组不抛（不做规范断言，仅记录）。
