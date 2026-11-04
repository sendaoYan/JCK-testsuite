# URISyntaxException 设计文档（JDK-27）

> 覆盖目标：白盒结构 + 黑盒行为 + 完整契约矩阵。2026-11-05 深化批次 A：
> 18 → 43 断言。

## 一、API 功能

- 构造器（2）：`(String input, String reason)`、`(String input, String reason, int index)`。
- 读取：`getInput()`、`getReason()`（即 super.getMessage()）、`getIndex()`（未给为 -1）。
- `getMessage()` 重写拼装：`reason` +（index > -1 时 `" at index N"`）+ `": "` + `input`。
- 继承 Exception（受检），serialVersionUID = 2137979680897488891L，串行字段 input/index 共 2 个。

## 二、Java 用户如何使用

`new URI(spec)` 解析失败时捕获，读取 getIndex() 定位错误位置、getReason() 取原因。

## 三、开源代码实际用法调研 / 参考来源

- Oracle Java SE javadoc（java/net/URISyntaxException.html）。
- OpenJDK `java.base/java/net/URISyntaxException.java`（校验顺序：null→NPE 先于 index→IAE）。
- 探针实测双 JDK：消息格式、serialVersionUID 完全一致，本类无版本差异。

## 四、测试用例设计（映射到 URISyntaxExceptionTest.java）

| 测试点 | 方法 |
|---|---|
| 白盒：public/extends Exception/2 构造器/4 getter 反射存在 | testWhiteBox |
| 黑盒：基本三元组 + index 默认 -1 + 消息包含 reason/input | testBlackBox |
| getMessage 精确格式（`"missing :: bad input"`、`"r at index 0: in"`） | testContractMatrix |
| null 校验矩阵（input/reason/双 null → NPE；NPE 优先于 IAE） | testContractMatrix |
| index < -1 → IAE 且 message null | testContractMatrix |
| getCause null / initCause 一次性 | testContractMatrix |
| 真实解析失败 `new URI("1abc:x")` 精确三元组 | testContractMatrix |
| serialVersionUID + 2 串行字段 + round-trip 保三字段与消息 | testContractMatrix |

## 五、易错规范细节

- 消息拼装的 `": "` 在 reason 之后：reason `"missing :"` 产生 `"missing :: bad input"`。
- index == -1 不打印 index 段；index == 0 打印。
- 双 null 一律 NPE；负 index 只在 3 参构造器校验且晚于 null 检查。
