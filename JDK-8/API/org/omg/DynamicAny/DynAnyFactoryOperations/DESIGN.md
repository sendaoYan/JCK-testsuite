# org.omg.DynamicAny.DynAnyFactoryOperations

## API 功能
工厂操作方法集（create_dyn_any/create_dyn_any_from_type_code）。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- 恰好 2 个方法；不继承 DynAnyOperations；异常签名含 InconsistentTypeCode。
