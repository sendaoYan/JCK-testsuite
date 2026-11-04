# java.security.IdentityScope — 设计案

## 1. API 功能
身份作用域抽象类（deprecated，extends Identity）：构造 `(String)`、`(String,IdentityScope)`（protected 无参）；抽象方法 `size/getIdentity(String)/getIdentity(PublicKey)/addIdentity/removeIdentity/identities`；具体方法 `getIdentity(Principal)`（按名委托）；静态 `getSystemScope()`/`setSystemScope(IdentityScope)`（后者 protected static）。

## 2. 使用方式
自定义 Map 型子类管理 Identity 集合；`(String,scope)` 构造时把自己 addIdentity 进父 scope；系统级作用域经 setSystemScope 安装（测试必须还原）。

## 3. 参考来源
- javadoc `java.security.IdentityScope`。
- 实测（JDK-8）：getSystemScope() 默认 null；getIdentity((Principal)null)→NPE；嵌套构造通知父 scope 恰一次。

## 4. 测试用例设计 + 易错规范细节
- 结构元数据：恰 6 个抽象方法；getIdentity(Principal) 非 abstract（按名委托）；get/setSystemScope static、setSystemScope protected；无参构造 protected。
- 构造矩阵：(String) 名字/无父 scope/空；(String,scope) getScope==parent 且 parent 收到 child（size 1、按名可查）。
- 委托语义：getIdentity(Principal) 命中按名/未命中 null/null 参数→NPE。
- 记录型 MapScope 语义：add 后 size/按名可查；重复名 add→KME；remove 后消失/再 remove→KME；identities() 枚举全成员（排序后断言）；空 scope 空枚举；toString 含名。
- 系统作用域：默认 null → setSystemScope 后可见 → finally 还原并断言还原成功（不得污染其它测试）。
- 继承与序列化：scope 本身是 Identity/Principal/Serializable；MapScope round-trip 后 name/size/按名查找仍可用。
- 易错点：setSystemScope 是 protected static——须在 IdentityScope 子类的静态方法体内调用；equals 继承 Identity 的按名语义，两个同名 scope 视为相等。
