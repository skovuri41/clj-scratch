(ns clj-scratch.destructing)

(defn configure [val & {:keys [debug verbose]
                        :or {debug false, verbose false}}]
  (println "val =" val " debug =" debug " verbose =" verbose))

(configure 10)

(def human {:person/name "Franklin"
            :person/age 25
            :hobby/hobbies "running"})
(let [{:keys [:person/name :person/age :hobby/hobbies]} human]
  (println name "is" age "and likes" hobbies))

(defn f-with-options
  [a b & {:keys [opt1]}]
  (println "Got" a b opt1))

(f-with-options 1 2 :opt1 true)
(f-with-options 1 2 :opt1 true)


(def john-smith {:f-name "John"
                 :l-name "Smith"
                 :phone "555-555-5555"
                 :address {:street "452 Lisp Ln."
                           :city "Macroville"
                           :state "Kentucky"
                           :zip "81321"}
                 :hobbies ["running" "hiking" "basketball"]
                 :company "Functional Industries"
                 :title "Sith Lord of Git"})


(defn print-contact-info
  [{:keys [f-name l-name phone company title]
    {:keys [street city state zip]} :address
    [fav-hobby second-hobby] :hobbies}]
  (println f-name l-name "is the" title "at" company)
  (println "You can reach him at" phone)
  (println "He lives at" street city state zip)
  (println "Maybe you can write to him about" fav-hobby "or" second-hobby))

(print-contact-info john-smith)
