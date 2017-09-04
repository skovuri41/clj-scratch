(ns clj-scratch.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(s/def ::id string?)
(s/valid? ::id "ABC-123")

(def id-regex #"^[0-9]*$")
(s/def ::id int?)
(s/def ::id-regex
  (s/and
   string?
   #(re-matches id-regex %)))
(s/def ::id-types (s/or ::id ::id-regex))

(s/valid? ::id-types "12345")
(s/valid? ::id-types 12435)
(s/valid? ::id 12435)

{::name "Brad" ::age 24 ::skills '()}
(s/def ::name string?)
(s/def ::age int?)
(s/def ::skills list?)

(s/def ::developer (s/keys :req [::name ::age]
                           :opt [::skills]))

(s/valid? ::developer {::name "Brad" ::age 24 ::skills '()})

{:name "Brad" :age 24 :skills '()}
(s/def ::developer (s/keys :req-un [::name ::age]
                           :opt-un [::skills]))
(s/valid? ::developer {:name "Brad" :age 24 :skills '()})

(s/explain ::id-types "Wrong!")

(gen/generate (s/gen int?))
(gen/generate (s/gen ::developer))

;; If you don't want to use the registry, you can actually avoid defining
;; the spec and just check the validity directly.

(s/valid? int? 1234)



;; clojure spec

(s/def ::even? (s/and integer? even?))
(s/def ::odd? (s/and integer? odd?))
(s/def ::a integer?)
(s/def ::b integer?)
(s/def ::c integer?)
(def s (s/cat :forty-two #{42}
              :odds (s/+ ::odd?)
              :m (s/keys :req-un [::a ::b ::c])
              :oes (s/* (s/cat :o ::odd? :e ::even?))
              :ex (s/alt :odd ::odd? :even ::even?)))

(s/conform s [42 11 13 15 {:a 1 :b 2 :c 3} 1 2 3 42 43 44 11])
{:forty-two 42, :odds [11 13 15], :m {:a 1, :b 2, :c 3}, :oes [{:o 1, :e 2} {:o 3, :e 42} {:o 43, :e 44}], :ex [:odd 11]}

(s/valid? nil? nil)  ;; true
(s/valid? string? "abc")  ;; true

(s/valid? #(> % 5) 10) ;; true
(s/valid? #(> % 5) 0) ;; false

(import java.util.Date)
(s/valid? inst? (Date.))  ;; true

(s/valid? #{:club :diamond :heart :spade} :club) ;; true
(s/valid? #{:club :diamond :heart :spade} 42) ;; false

(s/valid? #{42} 42)

(s/def ::date inst?)
(s/def ::suit #{:club :diamond :heart :spade})

(s/valid? ::date (Date.))
;;=> true
(s/conform ::suit :club)

(use 'clojure.repl)

(doc ::date)


(gen/generate (s/gen int?))
(gen/generate (s/gen nil?))
(gen/sample (s/gen string?))
(gen/sample (s/gen #{:club :diamond :heart :spade}))
(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))


(s/exercise (s/cat :k keyword? :ns (s/+ number?)) 5)

(s/exercise (s/or :k keyword? :s string? :n number?) 5)

(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- end start)))))

(s/fdef ranged-rand
        :args (s/and (s/cat :start int? :end int?)
                     #(< (:start %) (:end %)))
        :ret int?
        :fn (s/and #(>= (:ret %) (-> % :args :start))
                   #(< (:ret %) (-> % :args :end))))

(doc ranged-rand)



(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/and string? #(re-matches email-regex %)))

(s/def ::acctid int?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::email ::email-type)

(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))


(s/valid? ::person
          {::first-name "Elon"
           ::last-name "Musk"
           ::email "elon@example.com"})

(s/explain ::person
           {::first-name "Elon"})

(s/explain ::person
           {::first-name "Elon"
            ::last-name "Musk"
            ::email "n/a"})

(defn person-name
  [person]
  {:pre [(s/valid? ::person person)]
   :post [(s/valid? string? %)]}
  (str (::first-name person) " " (::last-name person)))

(person-name 42)
(person-name {::first-name "Elon" ::last-name "Musk" ::email "elon@example.com"})

(defn person-name
  [person]
  (let [p (s/assert ::person person)]
    (str (::first-name p) " " (::last-name p))))

(s/check-asserts true)
(person-name 100)



defn- set-config [prop val]
;; dummy fn
(println "set" prop val))


(s/def ::config (s/*
                 (s/cat :prop string?
                        :val  (s/alt :s string? :b boolean?))))
(s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])

(defn- set-config [prop val]
  ;; dummy fn
  (println "set" prop val))

(defn configure [input]
  (let [parsed (s/conform ::config input)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data ::config input)))
      (for [{prop :prop [_ val] :val} parsed]
        (set-config (subs prop 1) val)))))

(configure ["-server" "foo" "-verbose" true "-user" "joe"])
(configure [123 "foo" "-verbose" true "-user" "joe"])


;;
(s/def :animal/kind string?)
(s/def :animal/says string?)
(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))
(s/def :dog/tail? boolean?)
(s/def :dog/breed string?)
(s/def :animal/dog (s/merge :animal/common
                            (s/keys :req [:dog/tail? :dog/breed])))
(s/valid? :animal/dog
          {:animal/kind "dog"
           :animal/says "woof"
           :dog/tail? true
           :dog/breed "retriever"})


;; ;;;

(s/conform even? 1000)
(s/conform even? 999)

(s/valid? even? 10)

(s/valid? nil? nil)
(s/valid? #(> % 5) 10)
(s/valid? #{:club :diamond :heart :spade} :club)

;; registry

(s/def ::date #(instance? js/Date %))
(s/def ::suit #{:club :diamond :heart :spade})
(s/valid? ::date (js/Date.))
(s/conform ::suit :club)

(s/def ::big-even (s/and integer? even? #(> % 1000)))
(s/valid? ::big-even :foo)

(s/valid? ::big-even 10)
(s/valid? ::big-even 100000)

(s/def ::name-or-id (s/or :name string?
                          :id   integer?))
(s/valid? ::name-or-id "abc")

(s/valid? ::name-or-id 100)
(s/valid? ::name-or-id :foo)
(s/conform ::name-or-id "abc")
(s/conform ::name-or-id 100)

(s/valid? string? nil)
(s/valid? (s/nilable string?) nil)

(with-out-str
  (s/explain ::suit 42))
(s/explain-data ::big-even 5)

(s/explain-str ::name-or-id :foo)
