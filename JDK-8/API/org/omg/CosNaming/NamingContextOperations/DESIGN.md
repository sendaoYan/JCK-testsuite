# org.omg.CosNaming.NamingContextOperations

## API 功能
NamingContext 的纯操作方法集（IDL Operations 接口）。
## 使用方式
被 NamingContext/_NamingContextStub/NamingContextPOA 共同继承。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- **恰好 10 个**声明方法（getDeclaredMethods 数量锁死）；destroy 只声明 NotEmpty；list 无 checked 异常；不继承 CORBA.Object。
