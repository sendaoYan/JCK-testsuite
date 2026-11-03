# java.security.cert.PolicyNode 黑盒行为测试设计

## 1. API 功能
策略树节点接口：getParent/getChildren/getDepth/getValidPolicy/getPolicyQualifiers/getExpectedPolicies/isCritical。

## 2. Java 用户如何使用
PKIX 策略处理结果的遍历面。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/PolicyNode/GetPolicyQualifiers.java（真实遍历用例）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：真实 builder（target==anchor）产生非空树：根 getParent==null、depth==0、validPolicy=="2.5.29.32.0"（anyPolicy）、qualifiers 空、isCritical==false、expectedPolicies=={2.5.29.32.0}（Set，实测）、单证书链无子节点；子节点 depth/parent 链关系断言模板。

断言入口：`PolicyNodeTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
