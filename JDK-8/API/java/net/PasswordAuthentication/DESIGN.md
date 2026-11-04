# PasswordAuthentication 设计文档

## 一、API 功能

`java.net.PasswordAuthentication` 不可变凭据载体：`String userName` + `char[] password`。final 类、单一构造器、两个存取器。构造时**克隆**传入的 char[]；`getPassword()` 返回**内部引用**（文档明示调用方须自行 clone）。

## 二、Java 用户如何使用

`Authenticator` 子类回调中 `return new PasswordAuthentication(user, password)`；接收方（协议栈）调 `getUserName()`/`getPassword()` 取值，用后清零数组。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.PasswordAuthentication` 源码（构造器 `this.password = password.clone()`；getPassword 直接返回字段）；类文档 "It is the responsibility of the caller to clone the password if it will be modified"。

## 四、测试用例设计（106 断言）

- **白盒**：final、单构造器、返回类型 String/char[]、**非 Serializable**。
- **构造防拷**：改入参数组不影响内部（空/1 字符/1000 字符/Unicode）。
- **getPassword 引用语义**：两次调用同一引用；改返回数组影响内部（JDK 实际行为）；调用方 clone 隔离。
- **用户名矩阵**：null/空/带空白/Unicode/at-sign 均接受并原样返回。
- **负例**：null password 抛 NPE（JDK-8 消息为裸 "null"，只断类型）。
- **对象语义**：无 equals/hashCode 覆写——身份相等；toString 含类名。
- **Authenticator 流程往返**：经 requestPasswordAuthentication 回传同一实例。
- **组合矩阵**：5 用户 × 4 口令 × 3 断言 + 无别名验证。

## 五、易错规范细节（探针实证）

- 构造器克隆入参、getPassword **不**克隆返回值——两个方向不对称，文档只警示后者。
- null 用户名合法（getUserName 返回 null）；空口令数组合法（length 0）。
- 不可 Serializable：序列化会抛 NotSerializableException。
- equals 未覆写：两个内容相同的实例互不相等——依赖引用身份。

## 六、版本差异（JDK-8 vs JDK-27）

- 语义完全一致；唯一差异是 JDK-27 的 NPE 带有帮助性消息（含 "password" 字样），JDK-8 为裸 "null"。测试分别按类型/消息匹配。
