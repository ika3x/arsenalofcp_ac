merge(x, y) -> int: x,yをマージし、処理後の時刻を返す (既に同じ集合ならcurrentTimeが進まないため、現在の時刻と全く同じ値が返ることに注意する)
size(x, t) -> int: 時刻tにおけるxを含む集合のサイズ
same(x, y, t) -> boolean: 時刻tにおいてx,yが同じ集合か
leader(x, t) -> int: 時刻tにおけるxの根
latestTime() -> int: すでに起こった中で最新のマージ処理が行われた時刻を返す

計算量
construct: O(n)
merge: O(log n)
find: O(log n)
same: O(log n)
leader: O(log n)