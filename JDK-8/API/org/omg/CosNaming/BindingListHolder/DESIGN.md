# org.omg.CosNaming.BindingListHolder

## API 功能
Binding[]（BindingList typedef）Holder——list() 第一出参。
## 使用方式
默认 null；_type() 为 **tk_alias**（typedef 的 Holder 保留别名形态）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 数组元素逐项往返；holder._read 消费 BindingListHelper.write 的流。
