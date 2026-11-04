# java.net.http.HttpResponse.PushPromiseHandler.PushId.Http3PushId 设计文档

## 1. API 功能

record `Http3PushId(long pushId, String connectionLabel)`，实现 sealed 接口 `PushId`。

## 2. 使用方式

只读访问器 `pushId()`、`connectionLabel()`；作为 PushId 在集合/映射中按值使用。

## 3. 参考来源

- JDK-27 `javap`；`PushPromiseHandler.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`Http3PushIdTest.java`。

- record 形状：2 个分量 pushId(long)/connectionLabel(String)、规范构造器 public。
- toString 精确格式 `Http3PushId[pushId=42, connectionLabel=conn-1]`（probed）；null label 允许且打印
  `connectionLabel=null`（probed）；0/空/Long.MIN_VALUE/Long.MAX_VALUE 均合法。
- equals 对称、null/异类 false、单分量差异即不等；hashCode 一致。
- Set/Map 按值查找；经 PushId 父类型调用 toString。
- 序列化（probed）：record 未实现 Serializable，writeObject → NotSerializableException。

## 5. 版本差异

- JDK-27 新增类型；早期版本不存在。
