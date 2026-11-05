# ORBInitInfoPackage.ObjectIdHelper 契约设计

## API 功能
别名 Helper（**string** 版 ObjectId——与 PI 包内 byte[] 版不同）。

## 参考来源
- OMG PI ORBInitInfo；jdk8u372-b07 实测。

## 测试用例设计
- id 精确/tk_alias/wire String 往返/Any 往返。
- 反射对照：PI.ObjectIdHelper.write(OutputStream, byte[]) vs 本类 write(OutputStream, String)。

## 易错规范细节
- 同名类跨包类型差异是最易错点。
