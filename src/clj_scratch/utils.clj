(ns clj-scratch.utils
  (:require [clojure.string :as string]
            [clojure.walk :as walk]))
        
        
(defn transform-keys
  "Recursively transforms all map keys in coll with t."
  [t coll]
  (let [f (fn [[k v]] [(t k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) coll)))
