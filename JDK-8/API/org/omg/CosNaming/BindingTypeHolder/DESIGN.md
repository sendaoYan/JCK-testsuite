# org.omg.CosNaming.BindingTypeHolder

## API 功能
BindingType 枚举 Holder（out/inout 参数容器）。
## 使用方式
默认 value==null；带参构造；_read/_write；_type() tk_enum。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- wire 往返校验 **== 同一常量对象**（归一化）；两个常量分别过流；_type 与 BindingTypeHelper.type() equal。
