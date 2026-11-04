# java.nio.file.PathMatcher 测试方案

## 1. API 功能
java.nio.file.PathMatcher 是函数式接口: boolean matches(Path path)。
JDK-8 获取方式 (FileSystem.getPathMatcher(String syntaxAndPattern))：
- "glob:" 语法: * ? [abc] [!abc] {a,b} ** 跨分隔符；按路径分隔符为边界的匹配。
- "regex:" 语法: java.util.regex 语义，作用于整个路径字符串。
- 未知语法 -> UnsupportedOperationException；缺 ":" 或空 -> IllegalArgumentException (实测)。

## 2. Java 用户如何使用
- 常规用法: fs.getPathMatcher("glob:*.java") 后对遍历到的 Path 调 matches 过滤。
- 自定义: lambda 实现 PathMatcher 做任意谓词 (与工厂产物可混用)。

## 3. 开源代码实际用法调研
- 通用形态: 文件扫描器以 glob 表达用户意图 ("**/*.java" 递归源文件)；正则用于精确路径形状。

## 4. 测试用例设计 + 自动化实现
文件: `PathMatcherTest.java` (testWhiteBox + testBlackBox + testGlobMatrix + testRegexMatrix +
testFactoryNegatives，main 依次调用)。

白盒 (反射): 接口；非泛型；has matches(Path)；函数式接口；matches 返回 boolean。

黑盒矩阵 (batch A9 探针 /tmp/nio-a9-probe/Probe6 双 JDK 实测):
- glob: *.java 锚定末尾不跨 /、大小写敏感、* 可匹配空；file?.txt 问号恰一字符；
  *.{java,txt} 备选；[abc]/[!abc] 单字符类；**.java 跨段；**/*.java 至少一层目录；
  glob:/tmp/*.txt 绝对路径；空模式 "glob:" 可构造且匹配空路径。
- regex: .*\.java 跨分隔符；^a$ 锚定；(a|b)+c 分组；a\.b 转义点；[/] 字符类含分隔符；
  空模式匹配空路径；Paths.get("foo/bar/") 尾随分隔符被规范化，不再满足尾随 / 的正则。
- 工厂负例精确类型 (双侧一致): 未知语法 "jck:" -> UnsupportedOperationException (javadoc 契约，
  非任务书猜测的 IAE)；null -> NPE；""/无冒号/":" -> IllegalArgumentException；
  畸形模式 "regex:[unclosed"/"glob:{unclosed" -> PatternSyntaxException (IAE 子类)。
- 工厂每次返回新实例且行为等价。

健壮性: 纯内存匹配无 IO；不依赖墙钟/随机；headless 不加 @key。
本机实测断言数: 63 条 (旧 3)。
