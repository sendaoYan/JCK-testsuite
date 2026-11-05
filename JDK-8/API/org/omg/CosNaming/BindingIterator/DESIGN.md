# org.omg.CosNaming.BindingIterator

## API 功能
绑定迭代器：next_one/next_n/destroy。
## 使用方式
list() 第二出参，客户端逐批拉取绑定。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- 三个方法签名精确（返回 boolean、Holder 参数类型）；层级三件套断言。
