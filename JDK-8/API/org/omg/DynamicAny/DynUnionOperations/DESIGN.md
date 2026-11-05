# org.omg.DynamicAny.DynUnionOperations

## API 功能
DynUnion 的 9 个自有方法。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- 恰好 9 方法；注意判别器 getter 名为 **get_discriminator**（无 set_to_* 之外的别名）。
