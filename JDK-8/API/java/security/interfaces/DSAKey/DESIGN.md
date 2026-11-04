# java.security.interfaces.DSAKey — 设计案

## 1. API 功能
DSA 密钥公共父接口：`getParams()` 返回 DSAParams 域参数。公私钥两侧（DSAPublicKey/DSAPrivateKey）都继承它；本身不继承 Key/Serializable。

## 2. 使用方式
生成 DSA 密钥后通过 `((DSAKey) key).getParams()` 统一读取 p/q/g 域参数，也可把参数包成 DSAParameterSpec 复用于下一次 initialize。

## 3. 参考来源
- javadoc `java.security.interfaces.DSAKey`（接口层级、getParams 契约）。
- OpenJDK `test/jdk/java/security/KeyFactory/` DSA 用例思路。

## 4. 测试用例设计 + 易错规范细节
- 白盒：接口声明 getParams、无父接口、不声明 serialVersionUID。
- 记录型最小实现：getParams 恒等透传（含 null 透传——接口不禁止）。
- 继承格：DSAKey 非 Key 非 Serializable；DSAPublicKey/DSAPrivateKey 可赋值给 DSAKey。
- 真实闭环（DSA-1024）：公私两侧均实现 DSAKey；两侧域参数逐字段相等（p 1024 位、q 160 位）；参数经 DSAParameterSpec 复用于再生成后逐字段不变。
- equals：自反、公私两侧不等、与记录实现不等。
- 易错点：getParams 每次调用可能返回新对象，只能断言 equals 不能断言 ==；DSA y/x 的位长随机会浮动（断言范围而非精确值）。
