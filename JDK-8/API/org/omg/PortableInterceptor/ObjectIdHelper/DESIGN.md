# org.omg.PortableInterceptor.ObjectIdHelper 契约设计

## API 功能
别名 Helper（octet 序列版 ObjectId）。

## 参考来源
- OMG PI；jdk8u372-b07 实测。

## 测试用例设计
- id 精确/tk_alias/wire 往返（含空序列）/Any 往返。
- 跨包对照：ORBInitInfoPackage.ObjectIdHelper 是 **String** 版本——用方法签名反射区分。

## 易错规范细节
- 两个同名不同包 ObjectIdHelper 类型不同（byte[] vs String），断言必须全限定名。
