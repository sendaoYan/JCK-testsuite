# java.security.spec.ECFieldF2m — 设计案

## 1. API 功能
二进制域 F2m 字段：三构造（normal basis `ECFieldF2m(m)`；多项式基 `(m, int[] ks)` 与 `(m, BigInteger rp)`）+ `getM/getFieldSize/getReductionPolynomial/getMidTermsOfReductionPolynomial`；equals/hashCode 值语义。

## 2. 使用方式
pentanomial ks={k1,k2,k3}（降序三项式中间指数，不含 x^0）、trinomial ks={k}；rp 为完整归约多项式（x^m、x^0 位与 1 或 3 个中间项）；normal basis 无 rp/midTerms（均 null）。

## 3. 参考来源
- javadoc 异常契约（m 非正→IAE；ks 长度非 1/3→IAE；降序/范围约束）。
- NIST B-163（x^163+x^7+x^6+x^3+1）、B-233（x^233+x^74+1）。

## 4. 测试用例设计 + 易错规范细节
- ks 矩阵（实测）：null→NPE；长度 0/2/4→IAE "length of ks is neither 1 nor 3"；{7,7,1} 非严格降序→IAE "values in ks are not in descending order"；ks[2]<=0→IAE "ks[2] is out of range"；单元素 {0}/{163}→IAE "ks[0] is out of range"；合法只需 0<k<m（{8,2,1}、{74} 合法）。
- rp 矩阵（实测）：null→NPE；bitLength!=m+1 / 缺 x^0 位 / 缺 x^m 位 / 高于 m 位 / 负数→IAE "rp does not represent a valid reduction polynomial"；B-163→midTerms [7,6,3]、B-233→[74]。
- 跨构造等价：rp 形式与 ks 形式表示同域时 equals/hashCode 相等（B-233 rp == ks{74}）。
- 防御拷贝：构造传入 ks 数组改后不影响；getMidTermsOfReductionPolynomial 每次返回新拷贝。
- normal basis：getReductionPolynomial()==null 且 midTerms==null；equals 仅对同 m normal 成立；与多项式基不等。
- 易错点：ks **不含尾 0**（{7,2,1,0} 抛 IAE，与 RFC/SEC 文档中带 x^0 的记法不同）；equals 跨 Fp 类型恒 false。
