(ns clj-scratch.string
  (:require [cuerdas.core :as str]))


(str/strip-tags "<p>just <b>some</b> text</p>")

(str/strip-tags "<p>just <b>some</b> text</p>" ["p"])

(str/parse-int "1.4")
;; => 1

(str/parse-int nil)


(str/parse-double "1.4")

(str/parse-double nil)

(str/pascal "my name is epeli")
;; => "MyNameIsEpeli"

(str/pascal :some-record)
;; => "SomeRecord"

(str/pascal nil)

(str/pad "1" {:length 8})
;; => "       1"

(str/pad nil {:length 8})
;; => nil

(str/pad "1" {:length 8 :padding "0"})
;; => "00000001"

(str/pad "1" {:length 8 :padding "0" :type :right})
;; => "10000000"

(str/pad "1" {:length 8 :padding "0" :type :both})

(str/strip-newlines "a\n\nb")

(str/kebab :favorite-bbq-food)
