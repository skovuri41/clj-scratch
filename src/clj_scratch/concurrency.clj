(ns clj-scratch.concurrency)

;; When you want to defer the evaluation of expressions until you obtain
;; values to pass to them, use `promise`. The easiest example why you
;; want to use promise is implementing a callback.

(def my-promise (promise))

(def listen-and-callback
  (fn []
    (println "Start listening...")
    (future
      (println
       "Callback fired: "
       @my-promise))))

(defn do-time-consuming-job []
  (Thread/sleep 5000)
  (deliver my-promise "delivered value"))

(comment
  (listen-and-callback)
  (do-time-consuming-job))
