(ns clj-scratch.pic)

;; Immutability
;; Referential transparency
;; First-class functions
;; Partial Application (and Currying)
;; Recursive iteration
;; Composability

;; Sequences
;; List Comprehension
;; Sequence Abstraction
;; Lazy Sequences
;; Destructuring
;; Pattern Matching
;; Polymorphism

;; Concurrency
;; Retriable
;; Coordinated
;; Asynchronous
;; Thread Safe
;; Delay
;; Promise
;; Future
;; Atom
;; Lock
;; Agent
;; Ref
;; Channels

;; Functions
;; Macros
;; Interning

;; Change the language via Macros
;; Writing your own Macros

;; OOP
;; defprotocol
;; deftype
;; defrecord
;; Reify


(conj '(1 2 3) 4) ;; => (4 1 2 3);; same as the more explicit cons
(conj [1 2 3] 4) ;; => [1 2 3 4]
(peek [1 2 3]) ;; => 3
(peek '(1 2 3)) ;; => 1
(pop [1 2 3]) ;; => [1 2]
(pop '(1 2 3)) ;; => (2 3)

(get [1 2 3 4 5] 3) ;; => the value 4
(assoc [1 2 3 4 5] 5 6) ;; => [1 2 3 4 5 6]
(pop [1 2 3 4 5]) ;; => [1 2 3 4]

(subvec [1 2 3 4 5] 1 3)

(defn drop-nth [n coll]
  (->> coll
       (map vector (iterate inc 1))
       (remove #(zero? (mod (first %) n)))
       (map second)))
(drop-nth 3 [:a :b :c :d]) ;; => (:a :b :d)

(get {:my-key "this is my value"} :my-key) ;; => "this is my value"

(assoc {:my-key "this is my value"} :new-key "some new value")

(dissoc {:foo "bar" :baz "qux"} :baz) ;; => {:foo "bar"}

(select-keys {:name "Mark" :age 33 :location "London"} [:name :location])


(get {:foo "bar" :baz "qux"} :baz) ;; => "qux"
(:baz {:foo "bar" :baz "qux"}) ;; => "qux"


(contains? {:foo "bar" :baz "qux"} :foo) ;; => true

(keys {:foo "bar" :baz "qux"}) ;; => (:baz :foo)
(vals {:foo "bar" :baz "qux"}) ;; => ("qux" "bar")

(sorted-set 3 1 2) ;; => #{1 2 3}

(apply sorted-set #{3 1 2}) ;; => #{1 2 3}

(conj #{1 2 3} 4);; => #{1 4 3 2}

(conj #{1 2 3} 3) ;; => #{1 3 2}

(def ^:dynamic my-name "Mark")
(prn my-name) ;; => "Mark"
(binding [my-name "Bob"]
  (prn my-name)) ;; => "Bob"
(prn my-name) ;; => "Mark"




(defn cap [s]
  (clojure.string/upper-case s))
(defn greeting [c s]
  (prn (c s)))
(greeting cap "hi there")


(defn sum [x y]
  (+ x y))
(def add-on-five (partial sum 5))
(add-on-five 10) ;; => 15


((complement empty?) "") ;; => false
(apply str ["a" "b" "c"]) ;; => "abc"
(map inc [1 2 3]) ;; => (2 3 4)

(map
 (fn [[k v]] (inc v))
 {:a 1 :b 2 :c 3}) ;; => (4 3 2)

(reduce + [1 2 3]) ;; => 6
(filter even? (range 10)) ;; => (0 2 4 6 8)
((comp clojure.string/upper-case (partial apply str) reverse) "hello")


(loop [i 10]
  (if (= i 0)
    (prn "finished")
    (recur (do (prn i) (dec i)))))

(defn count-down [x]
  (if (= x 0)
    (prn "finished")
    #(count-down (do (prn x) (dec x)))))
(trampoline count-down 10) ; works fine still
(trampoline count-down 100000) ; this works now and no longer triggers an error


(for [x (range 5)
      :when (> (* x x) 3)]
  (* 2 x)) ;; => (4 6 8)
;; => (4 6 8)

(let [[x y] [:a :b]]
  (prn y x)) ;; => :b :a

(let [{a :a b :b} {:a "A" :b "B"}]
  (prn a b)) ;; => "A" "B"

(let [{:keys [a b]} {:a "A" :b "B"}]
  (prn a b)) ;; => "A" "B"

(let [{:keys [a c] :as complete} {:a "A" :b "B" :c "C" :d "D"}]
  (prn a c complete)) ;; => "A" "C" {:c "C", :b "B", :d "D", :a "A"}


(def a-map {:a 1 :c 3})
(let [{:keys [a b c] :as original-data :or {a 11 b 22 c 33}} a-map]
  [a b c original-data]) ;; => [1 22 3 {:c 3, :a 1}]

(let [[x y & z] [:a :b :c :d]]
  (prn x y z)) ;; => :a :b (:c :d)



(defmulti foo :key)
(defmethod foo :a [this] (prn "A"))
(defmethod foo :b [this] (prn "B"))
(defmethod foo :c [this] (prn "C"))
(defmethod foo :default [this](prn (str "Sorry, no idea what to do with '" (:key this) "' ?")))
(foo {:key :a}) ;; => "A"
(foo {:key :b}) ;; => "B"
(foo {:key :c}) ;; => "C"
(foo {:key :d}) ;; => "Sorry, no idea what to do with ':d' ?"

(def counter (atom 0))
(swap! counter inc) ;; => 1

(def counter (atom 0))
(set-validator! counter #(even? %))
(swap! counter inc) ;; => IllegalStateException Invalid reference state clojure\
.lang.ARef.validate
(swap! counter #(+ 2 %)) ;; => 2
(swap! counter inc) ;; => IllegalStateException Invalid reference state clojure\
.lang.ARef.validate
(swap! counter #(+ 2 %)) ;; => 4


(defprotocol Foo
  (bar [this])
  (baz [this]))
(defrecord Qux [kA kB]
  Foo
  (bar [this] (prn (str "hi bar: " kA)))
  (baz [this] (prn (str "hi bar: " kB))))
(def qux (->Qux "a" "b"))
qux ;; => #user.Qux{:kA "a", :kB "b"}
(bar qux) ;; => "hi bar: a"
(baz qux) ;; => "hi bar: b"

(def qux (map->Qux {:kA "A!" :kB "B!"}))
qux ;; => #user.Qux{:kA "A!", :kB "B!"}
(:kB qux) ;; => "B!"

(def my-anonymous-object
  (reify
    Foo
    (bar [this] (prn (str "hi bar")))))
(bar my-anonymous-object) ;; => "hi bar"



(comment
  ;;  async -
  (def c (chan))
  (>!! c :foo) ;; will block until something
  ;; takes :foo out the other end of the channel
  (def c (chan))
  (future (>!! c :foo)) ;; this thread will be blocked
  (<!! c) ;; => :foo

  (future
    (prn (str "hey! a new value " (<!! c)))) ;; blocks in a child thread
  (>!! c "bar") ;; => true
  ;; side effect of printing: "hey! a new value bar"

  (def a (chan))
  (def b (chan))
  (def c (chan))
  (defn put-data [c n]
    (go (Thread/sleep (rand 10))
        (>! c (str "Hi " n))))
  (put-data a "A")
  (put-data b "B")
  (put-data c "C")
  (let [[result channel] (alts!! [a b c])]
    (prn "Result: " result)
    (prn "Channel: " channel))

  (def c (chan 5))
  (def v [:a :b :c :d])
  (dotimes [i 4]
    (>!! c (nth v i))
    (prn "Put " (nth v i) " into the channel. Next..."))

  (def s (chan (sliding-buffer 5)))
  (def d (chan (dropping-buffer 5)))
  (def v [:a :b :c :d :e :f :g :h :i :j])
  (dotimes [i 10]
    (>!! s (nth v i))
    (prn "Put " (nth v i) " into the 'sliding buffer' channel"))
  (dotimes [i 10]
    (>!! d (nth v i))
    (prn "Put " (nth v i) " into the 'dropping buffer' channel"))
  (<!! s) ;; => :f
  (<!! d) ;; => :a





  )
