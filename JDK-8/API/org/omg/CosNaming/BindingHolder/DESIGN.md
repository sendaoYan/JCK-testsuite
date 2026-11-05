# org.omg.CosNaming.BindingHolder

## API 功能
Binding 的 Holder（next_one 的 out 参数）。
## 使用方式
默认 null；带参构造；_read/_write；_type() tk_struct。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 同 NameComponentHolder 模式；往返含嵌套 NameComponent 数组与 BindingType 常量。
