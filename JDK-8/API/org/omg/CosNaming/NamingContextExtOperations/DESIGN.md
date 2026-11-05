# org.omg.CosNaming.NamingContextExtOperations

## API 功能
NamingContextExt 的 4 个自有操作。
## 使用方式
继承 NamingContextOperations。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- 恰好 4 个自有方法；to_url 双 checked 异常（InvalidAddress+InvalidName）。
