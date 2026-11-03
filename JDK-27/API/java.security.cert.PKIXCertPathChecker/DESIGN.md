# java.security.cert.PKIXCertPathChecker 黑盒行为测试设计

## 1. API 功能
检查器抽象基类：init/isForwardCheckingSupported/getSupportedExtensions/check(Certificate,Collection) 抽象 + check(Certificate) 具体 + clone。

## 2. Java 用户如何使用
用户继承实现自定义校验（如私有扩展）注入 PKIXParameters。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/ 自定义 checker 用例；PKIXRevocationChecker 是其子类（实测 instanceof）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：Recording 子类验证 init 前调用 check 抛异常、init(false) 后 check 移除已支持扩展（unresolvedCritExts 集合被消费）、单参 check 委托双参、init(true) 可被实现拒绝、clone 独立；CertPathValidator.getInstance("PKIX").getRevocationChecker() instanceof 本类。

断言入口：`PKIXCertPathCheckerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
