# org.omg.CosNaming.BindingIteratorOperations

## API 功能
迭代器操作方法集。
## 使用方式
被 BindingIterator/_BindingIteratorStub/BindingIteratorPOA 继承。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- 恰好 3 方法；destroy 无 checked 异常（与 NamingContext.destroy 不同）。
