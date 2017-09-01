(ns clj-scratch.macros)

(defmacro unless [test then]
  "Evaluates then when test evaluates to be falsey"
  (list 'if (list 'not test)
        then))

(unless false (println "false!!"))

(macroexpand '(unless false (println "hi")))
