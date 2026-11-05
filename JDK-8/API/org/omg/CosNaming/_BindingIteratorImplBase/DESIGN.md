# org.omg.CosNaming._BindingIteratorImplBase

## API 功能
BindingIterator 服务端骨架（DynamicImplementation 路线）。
## 使用方式
继承实现 next_one/next_n/destroy。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- abstract/父类/接口三断言；匿名子类 _ids() 单元素精确。
