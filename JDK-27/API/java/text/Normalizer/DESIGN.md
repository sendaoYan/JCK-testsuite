# java.text.Normalizer 测试方案

## 1. API 功能（按方法分组）

- 静态工具：`normalize(CharSequence, Form)`、`isNormalized(CharSequence, Form)`。
- 形式枚举：`Normalizer.Form`：`NFC`、`NFD`、`NFKC`、`NFKD`。
- final 工具类，无公共构造器。

## 2. Java 用户如何使用

- 调用：`Normalizer.normalize(text, Normalizer.Form.NFC)` 做 Unicode 规范化；`Normalizer.isNormalized(text, Form.NFD)` 判断是否已规范化。
- 语义：NFC=规范合成（canonical compose）、NFD=规范分解（canonical decompose）、NFKC/NFKD 额外做兼容（compatibility）分解（连字、全角、上标等）。
- 组合链路：对同一文本依次做 NFC/NFD 组合与分解往返、NFKC 兼容映射、`isNormalized` 判定、幂等性校验。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.Normalizer` 委托 `sun.text.normalizer.NormalizerBase`，实现 Unicode 标准规范。
- JDK 测试 `test/jdk/java/text/Normalizer/*.java`：大量 NFC/NFD/NFKC/NFKD 组合/分解断言。
- 通用模式：搜索/比较前 `Normalizer.normalize(s, Form.NFC)` 统一表示；密码/文件名规范化、重音不敏感比较用 NFD。

## 4. 测试用例设计 + 自动化实现

实现位置：`NormalizerTest.java`（`testWhiteBox()` 白盒 + `testCanonicalComposition()`/`testCompatibilityComposition()`/`testIsNormalized()`/`testIdempotentAndNull()` 黑盒）。

- 规范组合/分解：`é`（U+00E9）与 `e`+U+0301 的 NFC/NFD 往返与幂等。
- 兼容组合/分解：连字 `ﬃ`（U+FB03）→ `ffi`、全角 A → `A`、上标 2 → `2`；并验证 NFC 不做兼容映射。
- 判定：`isNormalized` 对组合/分解形式各取真假。
- 边界：NFC 幂等、`normalize(null)`/`isNormalized(null)` 抛 `NullPointerException`。
- 健壮性：纯文本固定、无 Locale/墙钟/随机/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 11 样本 × 4 Form 全表（é/组合 é/½/Ａ/ﬃ/Ω/が/か+濁点/A+重音/Å 组与预组），含长度与幂等性
- isNormalized 与 normalize().equals() 对每样本一致
- Form 枚举序 NFD=0/NFC=1/NFKD=2/NFKC=3、valueOf、null CharSequence/Form→NPE
- CharSequence 重载（StringBuilder）；空串/纯 ASCII 恒等
