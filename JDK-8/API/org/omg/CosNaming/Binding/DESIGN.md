# org.omg.CosNaming.Binding

## API 功能
命名绑定条目：`NameComponent[] binding_name` + `BindingType binding_type`（nobject/ncontext）。
## 使用方式
`new Binding()`（null/null）或全参构造；list() 返回 Binding[]；经 BindingHelper/BindingListHelper 过 Any/流。
## 参考来源
JDK-8 rt.jar idlj 生成类；CosNaming 规范；行为以 jdk8u372-b07 实测为准。
## 测试用例设计 + 易错细节
- 默认构造两字段均 null；全参构造数组**引用原样保存**（`==` 断言，无防御拷贝）、长度 1、成员字段与 binding_type 常量回读。
- equals/hashCode/toString 均**未覆写**（身份语义 + 默认 toString）——与 NameComponent 一致，勿按值相等写断言。
- 空数组合法（length==0）；字段可变。
