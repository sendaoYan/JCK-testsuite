# PasswordAuthentication 设计文档

## 一、API 功能

封装用户名与密码的不可变凭据对象(密码以 char[] 保存)。

## 二、Java 用户如何使用

用户在 Authenticator 子类中返回 new PasswordAuthentication(user, pass)。

## 三、开源代码实际用法调研

认证回调返回此类型给 JDK 网络栈。

## 四、测试用例设计

getUserName/getPassword/getChars；构造对输入密码做防御性拷贝、返回内部引用；equals 语义。

## 五、缺口分析（批次 H 深化追加）

原测试仅 9 断言。深化至 **113 断言**（jtreg Passed），新增覆盖：

- 构造防拷：空/1 字符/1000 字符/Unicode；getPassword 同引用复用、改返回数组影响内部、调用方 clone 隔离。
- 用户名矩阵：null/空/空白/Unicode/at-sign 原样返回。
- 对象语义：无 equals 覆写（身份相等）、toString 含类名、非 Serializable。
- Authenticator 流程往返（同一实例回传）。
- 组合矩阵 5 用户 × 4 口令 × 3 断言 + 入参无别名。

## 六、版本差异（JDK-27 实证）

- null password NPE：JDK-27 消息含 "password"（帮助性 NPE），JDK-8 裸 "null"——两侧分别按消息/类型断言。
- 其余语义与 JDK-8 完全一致。
