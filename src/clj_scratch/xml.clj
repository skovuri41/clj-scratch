(ns clj-scratch.xml
  (:require [clojure.data.xml :as dx]
            [xml-in.core :as xml :refer [tag= some-tag= attr=]]))

(def universe (dx/parse-str (slurp "resources/universe.xml")))

(xml/find-all universe [:universe :system :solar :planet])

(xml/find-first universe [:universe :system :solar :planet])

(xml/find-all universe [:universe :system :delta-orionis :δ-ori-aa1 :radius])

(xml/find-first universe [:universe :system :delta-orionis :δ-ori-aa1 :radius])

;; tag= finds child nodes under all matched tags
;; some-tag= finds child nodes under the first matching tag
;; attr= finds child nodes under all tags with attribute's key and value

(xml/find-in universe [(tag= :universe)
                       (tag= :system)
                       (tag= :solar)
                       (tag= :planet)
                       ])
(xml/find-in universe [(tag= :universe)
                       (tag= :system)
                       (tag= :solar)
                       (some-tag= :planet)
                       ])
(xml/find-in universe [(tag= :universe)
                       (tag= :system)
                       (some-tag= :solar)
                       (attr= :age "4.543")
                       ])

(def solar (xml/find-first universe [:universe :system :solar]))
(def system (xml/find-first universe [:universe :system]))


(zipmap (-> solar
            (xml/find-in
             [(tag= :planet)]))
        (->> solar
             (map (comp :age :attrs))))

(xml/find-first universe [:universe :system :delta-orionis :δ-ori-aa1 :mass])
(xml/find-first universe [:universe :system :delta-orionis :δ-ori-aa1 :radius])
(xml/find-first universe [:universe :system :delta-orionis :δ-ori-aa1 :surface-gravity])

(def aa1 (xml/find-first universe [:universe :system :delta-orionis :δ-ori-aa1]))
(xml/find-first aa1 [:mass])
(xml/find-first aa1 [:radius])
(xml/find-first aa1 [:surface-gravity])
