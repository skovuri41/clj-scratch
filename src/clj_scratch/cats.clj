(ns clj-scratch.cats
  (:require [cats.core :as m]
            [cats.builtin]
            [cats.context :as ctx]
            [cats.monad.either :refer :all]
            [cats.monad.exception :as exc]
            [cats.monad.maybe :as maybe]))

;; semigroup with mappend operation:
(m/mappend (maybe/just [1 2 3])
           (maybe/just [4 5 6]))

;; A Monoid is a Semigroup with an identity element (mempty).
(m/mappend (maybe/just [1 2 3])
           (maybe/nothing)
           (maybe/just [4 5 6])
           (maybe/nothing))

;; The Functor represents some sort of "computational context", and the abstraction consists of one unique function: fmap.
(m/fmap inc (maybe/just 1))
(m/fmap inc (maybe/nothing))


;; The Applicative Functor represents some sort of "computational context" like a plain Functor,
;; but with the ability to execute a function wrapped in the same context.
;; The Applicative Functor abstraction consists of two functions: fapply and pure.
(m/fapply (maybe/just (fn [name] (str "Hello " name))) (maybe/just "Alex"))
(m/fapply (maybe/nothing) (maybe/just "Alex"))


;; bind works much like a Functor but with inverted arguments.
;; The main difference is that in a monad, the function is responsible for wrapping a returned value in a context.
(m/bind (maybe/just 1) (fn [v] (m/return (inc v))))
(m/bind (maybe/nothing) (fn [v] (m/return (inc v))))

(right :valid-value)
;; => #<Right [:valid-value :right]>

(left "Error message")


(exc/try-on 1)
(type (exc/try-on 1))

;; => #<Success [1]>

(exc/try-on (+ 1 nil))
(type (exc/try-on (+ 1 nil)))
(exc/try-or-recover (+ 1 nil)
                    (fn [e]
                      (cond
                        (instance? NullPointerException e) 0
                        :else 100)))
(exc/try-or-else (+ 1 nil) 2)


(m/mlet [a (maybe/just 1)
         b (maybe/just (inc a))]
        (m/return (* a b)))

(some->> {:a 1 :b 3} :a vector (filter odd?) (map inc) first) ;;=> 2
(some->> {:a 1 :b 3} :b vector (filter even?) (map inc) first) ;;=> nil
(some->> {:a 1 :b 3} :c vector (filter even?) (map inc) first) ;;=> nil

(def safe-inc (fnil inc 0))

(safe-inc (get {:a 1 :b 2} :b)) ;=> 3
(safe-inc (get {:a 1 :b 2} :c)) ;=> 1

(comment
  ;; chaining if-let’s together like this is annoying:

  (if-let [x (foo)]
    (if-let [y (bar x)]
      (if-let [z (goo x y)]
        (do (qux x y z)
            (log "it worked")
            true)
        (do (log "goo failed")
            false))
      (do (log "bar failed")
          false))
    (do (log "foo failed")
        false))

  ;; It’s only mildly less annoying when using cats:
  @(m/mlet [x (if-let [v (foo)]
                (either/right v)
                (either/left))

            y (if-let [v (bar x)]
                (either/right v)
                (either/left))

            z (if-let [v (goo x y)]
                (either/right v)
                (either/left))]

           (m/return (qux x y z)))
  )

;; or is really handy for default values

(-> :c
    {:a 1 :b 2}
    (or 41)
    inc)

(-> :b
    {:a 1 :b 2}
    (or 41)
    inc)

;; The simple fix is to check for nil just like you would check for
;; Nothing. Clojure provides the if-some function to make this more
;; concise:

(if-some [it (get {:a 1 :b 2} :c)]
  (+ 1 it)
  nil)

(or 42 nil)
(or nil 42)


;; get has a default value built in:
(get {:a 1 :b 2} :b)
(get {:a 1 :b 2} :c 0)

;;;; Applicative

(defn make-something [^String lang]
  (condp = lang
    "es" (maybe/just (fn [name] (str "Hola " name)))
    "en" (maybe/just (fn [name] (str "Hello " name)))
    (maybe/nothing)))

(m/fapply (make-something "es") (maybe/just "john"))



(defn m-div
  [x y]
  (if (zero? y)
    (maybe/nothing)
    (maybe/just (/ x y))))

(m-div 1 2)
(m-div 1 0)


(maybe/from-maybe (maybe/nothing) 42)
(maybe/from-maybe (maybe/just 1) 42)
