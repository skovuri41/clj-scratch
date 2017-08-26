(ns clj-scratch.functions)

;; Reference @daveyarwood on github (20 cool clojure functions)
;;; 1) [into]

;; `into` does something really simple, but in a really nice, intuitive
;; way; it takes two collections and adds the contents of the second
;; collection "into" the first. Under the hood, it's basically just doing
;; `(reduce conj first-coll second-coll)`, but the very fact that this
;; function exists as a clojure.core function can make code so much
;; neater-looking and concise. I most commonly find myself using it to
;; convert a collection of key-value vectors into a map, like so:

(into {} [[:a 1] [:b 2] [:c 3] [:d 4]])

;; You can also use it to easily convert from one collection type to
;; another, as in the following examples:
(into [] (range 10))
(into (sorted-map) {:b 2 :c 3 :a 1})


;;; 2) [mapv] (added in Clojure 1.4)

;; Another simple helper function, `mapv` works just like `map`, except
;; that it returns a vector instead of a list.

(mapv #(* % 2) (range 10))

;;; 3) [pmap]

;; `pmap` is another variant of `map`; it works just like `map`, but it
;; can be useful for computationally intensive functions by coordinating
;; the application of the function over the items in the collection so
;; that they are executed in parallel.

;; Stealing an example from ClojureDocs:
;; create a function that simulates a long running process using Thread/sleep
(defn long-running-job [n]
  (Thread/sleep 3000) ; wait for 3 seconds
  (+ n 10))

;; notice that the total elapse time is almost 3 secs * 4
(time (doall (map long-running-job (range 4))))

;; notice that the total elapse time is almost 3 secs only
(time (doall (pmap long-running-job (range 4))))

;;; 4) [mapcat][mapcat]

;; While I'm on the subject of `map` variants, `mapcat` is a handy one
;; that takes the result of mapping a function (presumably one that
;; returns a collection) over a collection, and then applies `concat` to
;; the results. So it's really just a shortcut for `(apply concat (map
;; ...`


(defn single-double-triple [x]
  [(* x 1) (* x 2) (* x 3)])

(mapcat single-double-triple (range 10))


;;; 5) [empty][empty]

;; Not to be confused with
;; [empty?]
;; which returns whether or not a collection is empty, `empty` returns an
;; empty collection that is the same type as its argument:

(empty [1 2 3 4 5])
(empty (range 10))
(empty {:a 1 :b 2 :c 3})


;; This is mainly useful if you're trying to write a function that
;; returns the same type of collection as its argument. For example,
;; let's say we want to write a function that applies `inc` to each item
;; in a collection. If our definition was just `(map inc coll)`, our
;; function would return a list every time, regardless of the collection
;; type of the argument. We can fix this using `into` and `empty`:


(defn inc-each [coll]
  (into (empty coll)
        (map inc coll)))

(inc-each [1 2 3])
(inc-each #{1 2 3})

;; (This is a bit of a contrived example, and not a perfect one --
;;       `(inc-each '(1 2 3))` would actually return `(4 3 2)` due to the way
;;       that `into` works, and the fact that `conj`-ing items into a list adds
;;       them onto the front rather than the back. But you get the idea!)

;;; 6) [fnil][fnil]

;; `fnil` is a "function modifier" -- it takes any function, and returns
;; a modified version of that function that will replace its `nil`
;; arguments with a value of your choosing.

;; This is surprisingly handy, as it enables you to easily create
;; functions with default values, like this:


(defn favs [age food]
  (format "I'm %d years old and my favorite food is %s." age food))

(def favs-with-defaults
  (fnil favs 28 "waffles"))

(favs-with-defaults 64 "cranberries")
(favs-with-defaults nil "anchovy pizza")
(favs-with-defaults 16 nil)
(favs-with-defaults nil nil)

;; Another cool thing about `fnil` is that it enables a "hash-map with
;; default value" behavior. In Ruby, you can create a hash that has a
;; default value that will always be returned when trying to get the
;; value of a key that doesn't exist in the hash:

;; {% highlight ruby %}
;; CDs = Hash.new(0) # 0 is the default value

;; def add_cd(artist)
;; CDs[artist] += 1
;; end

;; add_cd "Montel Jordan"

;; p CDs
;;                                         ;=> {"Montel Jordan"=>1}


;; In Clojure, you can mimic this functionality using `fnil`:


(defn add-cd [cds artist]
  (update-in cds [artist] (fnil inc 0)))

(add-cd {} "TLC")
(reduce add-cd {} ["TLC" "Brandy" "TLC" "Mariah Carey" "TLC" "TLC"])


;; Because `({} "TLC")` returns `nil` and not 0, we use `fnil` to turn
;; `inc` into a function that works just like `inc` unless its argument
;; is `nil`, in which case it acts like it's working with `0`. This is
;; especially nice (and arguably better than default hash values in Ruby)
;; because we could define additional functions that also use `fnil` to
;; work with different default values, while still working with the same
;; hash-map; this gives us the possibility of different default semantics
;; for different functions, and without requiring that the hash-map be
;; "set up" in any way to have default values.

;;; 7) [juxt][juxt]

;; `juxt` takes two or more functions and returns a function that returns
;; a vector containing the results of applying each function on its
;; arguments. In other words, `((juxt a b c) x) => [(a x) (b x) (c x)]`.
;; This is useful whenever you want to represent the results of using 2
;; different functions on the same argument(s), all at once rather than
;; separately:


(def inc-and-double
  (juxt inc #(* % 2)))

(inc-and-double 5)


;; This function is especially powerful when dealing with multimethods,
;; as it provides a concise way to dispatch on multiple parameters:


(defmulti guess-fruit (juxt :color :size))

(defmethod guess-fruit [:red :small]
  [fruit]
  "cherry")

(defmethod guess-fruit [:green :large]
  [fruit]
  "watermelon")


;;; 8) [partial][partial]

;; `partial` is a neat little function that some might consider a
;; "functional programming must-have." It takes a function that takes a
;; specific number of arguments, and the values for only some of those
;; arguments as parameters, and returns a function that will take the
;; remaining parameters.


(def add-eight
  (partial + 8))
;; ; note: this is essentially the same thing as (def add-eight #(+ 8 %)),
;; ;       but perhaps more readable

(add-eight 7)


;; A common use for `partial` is to create a modified version of a
;; function with "default" arguments:


(defn greet [greeting person]
  (format "%s, %s!" greeting person))

(greet "Yo" "Chuck")

(def greet-chinese
  (partial greet "你好"))

(greet-chinese "Barbara")


;;; 9) [comp][comp]

;; `comp` provides a nice, succinct way of expressing the composition of
;; two or more functions. To steal an example from ClojureDocs:


(def concat-and-reverse (comp (partial apply str) reverse concat))
(concat-and-reverse "dave" "yarwood")


;;; 10) [iterate][iterate]

;; The next few functions provide easy ways to create infinite lazy
;; sequences. `iterate` takes a function and a starting value and returns
;; a lazy sequence consisting of the starting value, the function applied
;; to the starting value, the function applied again to that second
;; value, the function applied again to that third value, ad infinitum.


(take 5 (iterate (partial * 3) 20))

;; Among the many cool things you can do with `iterate` is the creation
;; of an infinite sequence of Fibonacci numbers. (example shamelessly
;;                                                        stolen from ClojureDocs):


(def lazy-fibs
  (map first (iterate (fn [[a b]] [b (+' a b)]) [0 1])))

(take 10 lazy-fibs)

;;; 11) [cycle][cycle]

;; `cycle` returns a lazy sequence of the items in a collection,
;; repeating in sequence forever. For instance, `(cycle "dave")` returns
;; the infinite sequence `(\d \a \v \e \d \a \v \e \d \a ...)` etc. Among
;; the things we can do with `cycle` is writing a function that produces
;; a list of all the rotations of a given collection:


(defn rotations [coll]
  (take (count coll) (partition (count coll) 1 (cycle coll))))

(rotations "dave")


;;; 12) [repeat][repeat]

;; When given only one argument, `repeat` returns a lazy sequence of an
;; infinite number of that thing given as an argument. When given a
;; number, it returns that number of that thing.

(take 5 (repeat \x))
(repeat 5 \x))

(into {} (map vector [55 144 292 344 393] (repeat :ok)))


;;; 13) [repeatedly][repeatedly]

;; `repeatedly` works just like `repeat`, except that it takes a function
;; instead of a value as the thing to repeat. It then calls the function
;; (which must take no arguments, and presumably has side effects)
;; repeatedly and returns a lazy sequence of its values.


(repeat 5 (rand-int 500))  ; essentially (repeat 5 one-particular-random-number)
(repeatedly 5 #(rand-int 500)) ; generates a new random number each time

;;; 14) [constantly][constantly]

;; This is an interesting function that returns a function which takes
;; any number of arguments and then ignores them and returns a particular
;; value of your choosing.


(map (constantly 42) (range 10))


;; This function will come in handy if you ever encounter a situation
;; where you're using a function that expects a function as an argument,
;; but you really want to provide a constant value instead of a function.

;; To give you a real-world example, I recently found `constantly` useful
;; when I was using [instaparse][instaparse] to construct and transform a
;; parse tree. Instaparse's transform function takes a map of node-types
;; (as keywords) to functions used to transform their contents. I had
;; nodes representing flats and sharps, their content being strings like
;; `"flat"`, `"b"` or `"f"` for flat, `"s"` or `"sharp"` for sharp. I
;; wanted each node to be transformed into the strings `"-flat"` or
;; `"-sharp"`, regardless of what variation was parsed as a flat or sharp
;; node.


(defn parse-scale [input]
  (->> input
       (insta/parse (insta/parser "... grammar here ..."))
       (insta/transform
        {:flat (constantly "-flat")
         :sharp (constantly "-sharp")
         ...})))


;;; 15) [get-in][get-in]

;; The next few functions are useful whenever you're working with nested
;; data structures. `get-in` provides an easy way to obtain values from
;; within a nested collection:


(def djy
  {:name {:first-name "Dave"
          :last-name "Yarwood"}
   :age 28
   :hobbies ["music" "languages" "programming"]})

(get-in djy [:name :last-name])

(get-in djy [:hobbies 2])
;; As you can see, this works not only with maps, but also with vectors,
;; which act like maps with indices as keys.

;; Just like with `get`, you can supply any value as a final argument to
;; `get-in` in order to return it (instead of nil) if the specified key
;; is not found:
(get-in djy [:name :middle-name])
(get-in djy [:hobbies 3] :not-found)

;;; 16) [assoc-in][assoc-in]
;; You can use `assoc-in` to return a modified version of a nested data
;; structure with one of the values changed:
(assoc-in djy [:age] 29)
;; As with `assoc`, you can use `assoc-in` to create fields that didn't already exist:
(assoc-in djy [:address :city] "Durham")

;;; 17) [update-in][update-in]

;; `update-in` is a lot like `assoc-in`, except that instead of providing
;; a value, you provide a function to transform the existing value at a
;; particular place in your nested date structure.


(update-in djy [:age] inc)

;;; 18) [read-string][read-string]

;; These next three functions are fun and a little dangerous if you
;; aren't careful. They can be useful in metaprogramming contexts,
;; although typically macros will provide a safer and more flexible way
;; to do whatever you're trying to do.

;; `read-string` takes a string as an argument and reads one object from
;; the string. The contents of the string are assumed to be valid Clojure
;; code.


(read-string "42")
(read-string ":a")
(read-string "\"o hai\"")

;; Of note, `read-string` will compile an object, but will not evaluate
;; it. If you give it a string like `"(+ 1 1)"`, `read-string` will read
;; it in as a list containing `+`, `1` and `1`.

(read-string "(+ 1 1)")

;;; 19) [eval][eval]

;; `eval` evaluates a form data structure (i.e. a list of functions and
;; arguments that can be run as Clojure code) and returns the result.

(eval '(+ 1 1))
(eval '(println "yo!"))
(eval '(let [a 10] (+ 3 4 a)))
(eval (read-string "(+ 1 1)"))

;;; 20) [load-string][load-string]

;; `load-string` essentially does the same thing as `(eval (read-string
;; ...`, except that `read-string` will only read one object from a
;; string, whereas `load-string` will sequentially read and evaluate the
;; set of forms contained within a string.


(load-string "(println \"Adding 2 and 2 together...\") (+ 2 2)")
