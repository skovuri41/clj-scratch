(ns clj-scratch.loop)

(for [x [0 1 2 3 4 5]
      :let [y (* x 3)]
      :when (even? y)]
  y)

;; `while` modifier stops the evaluation of the body when the predicate is false.
(for [x (range 10) :while (not= x 5)]
  x)

(for [x ['a 'b 'c]
      y [1 2 3]]
  [x y])

(for [x '(-1 1 2) :when (< 0 x)]
  x)


(drop-while neg? [-3 -2 -1 0 1 2 -4 3])

(def l [-300 -2 -1 0 1 2 -4 3])
(conj (into [] (first l)) (drop-while neg? (rest [-3 -2 -1 0 1 2 -4 3])))
(conj (drop-while neg? (rest l)) (first l))
(conj (remove neg? (rest l)) (first l))

( -> []
 (conj 1)
 (conj 2)
 (conj 3))
