# org.omg.DynamicAny.DynAny

## API 功能
动态 Any 主接口（DynAnyOperations + CORBA.Object + IDLEntity）。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- extends 列表**逐项精确**（toString 断言）；24 个核心方法名全表存在。
