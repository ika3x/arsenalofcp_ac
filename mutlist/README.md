# MutList (Int/Long/Double)
`MutList` は、Java標準の `ArrayList<T>` で発生するオートボクシング（プリミティブ型 → ラッパー型への変換）のオーバーヘッドを排除した、メモリ効率と実行速度を最優先した可変長リストです。

大量の数値を保持する際に高速化が見込めます。

## 使い方
`IntMutList`, `LongMutList`, `DoubleMutList` を用途に合わせて使い分けてください。

使用例
```Java
LongMutList list = new LongMutList();

// 要素の追加
list.add(100L);
list.add(200L);

// 要素の取得・操作
long val = list.get(0);      // 100
list.set(0, 999L);           // 0番目を999に変更
long last = list.pollLast(); // 200を削除して取得

// ソートと二分探索
list.sort();
int index = list.binarySearch(999L);
```

ほとんどの仕様は `ArrayList` に基づいています。