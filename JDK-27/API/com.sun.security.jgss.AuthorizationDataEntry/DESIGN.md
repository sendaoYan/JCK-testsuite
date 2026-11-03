# com.sun.security.jgss.AuthorizationDataEntry 测试设计

## 1. API 功能
- 类型：`public final class AuthorizationDataEntry`（模块 `jdk.security.jgss`），表示 Kerberos 5 AuthorizationData 条目。
- 构造器：`AuthorizationDataEntry(int type, byte[] data)`——保存 `type`，并把 `data` 做一次 `clone()` 拷贝存入对象（`data` 为 null 时抛 `NullPointerException`；type 不做范围校验，0/负数均合法）。
- 查询方法：`int getType()`；`byte[] getData()`——返回内部数据的**新拷贝**（每次调用都 clone）；`String toString()`——含 `type=` 与字节数。

## 2. Java 用户如何使用
```java
import com.sun.security.jgss.AuthorizationDataEntry;
byte[] ad = new byte[]{1, 2, 3};
AuthorizationDataEntry e = new AuthorizationDataEntry(1, ad);
int t = e.getType();          // 1
byte[] copy = e.getData();    // 内容 {1,2,3}，但与 ad 不是同一数组
System.out.println(e.toString());
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/AuthorizationDataEntry.java`，构造器 `this.data = data.clone()`、`getData()` 返回 `data.clone()`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/sun/security/krb5/KrbCredSubKey.java`、`ServiceCredsCombination.java` 等 krb5 测试引用该类型（作为授权数据条目）。
- 第三方框架基本不直接用该内部 GSS 辅助类，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`AuthorizationDataEntryTest.testBlackBox()`）覆盖：
- `getType()` 返回构造时传入值（42）。
- `getData()` 内容与输入一致但非同一数组；修改返回值不影响内部状态；修改输入不影响已构造对象；两次 `getData()` 返回不同数组。
- type 为 0 / 负数均被接受；空 byte[] 得到长度 0。
- `toString()` 非空且含 `type=7`。
- `new AuthorizationDataEntry(1, null)` 抛 `NullPointerException`。
白盒部分补齐：具体类/final/方法 getType/getData/toString 存在。
