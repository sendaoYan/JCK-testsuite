# CodeSource — 设计案

## 1. API 功能
代码来源（implements Serializable）。构造器(URL, Certificate[]) 或 (URL, CodeSigner[]) + getLocation/getCertificates/getCodeSigners/implies/equals/hashCode/toString。

## 2. 使用方式
`new CodeSource(new URL("file:/x"), (Certificate[])null)` 或带证书数组。

## 3. 参考来源
- javadoc `java.security.CodeSource`。
- JCK runtime-8b `api/java_security/CodeSource/`。

## 4. 测试用例设计 + 易错规范细节
- 证书↔签名者双向转换：Certificate[] 构造→getCodeSigners（certPath 含证书、timestamp null）；CodeSigner[] 构造→getCertificates。
- null 语义：null URL 合法（getLocation null）；null 证书/签名者→getCertificates/getCodeSigners 均为 null；空 CodeSigner[]→空数组（非 null）。
- implies 非对称：无证书者 implies 有证书者（URL 相同）为 true；反向为 false；URL 不同 false。
- equals/hashCode 值语义；证书 vs 无证书不相等。
- 构造器对证书数组做防御性拷贝。
- 序列化往返保留 location 与 certificates。
- 易错点：java.security.cert.Certificate 与 java.security.Certificate 名称冲突需全限定。
