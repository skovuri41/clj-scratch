(ns clj-scratch.ems
  (:require
   [mount.core :refer [defstate]]
   [clojure.tools.logging :as log]
   [clojure.string :as string]
   [arka.config :refer [env]])
  (:import (java.util Enumeration Hashtable)
           (com.tibco.tibjms TibjmsQueueConnectionFactory TibjmsConnectionFactory)
           (javax.jms Message JMSException Session
                      Queue QueueBrowser
                      QueueConnection QueueReceiver
                      QueueSession QueueSender)
           (javax.naming Context InitialContext)))

(defn- initial-context
  "initial context"
  [{:keys [jndi-url username password]}]
  (let [env (doto (Hashtable.)
              (.put Context/INITIAL_CONTEXT_FACTORY "com.tibco.tibjms.naming.TibjmsInitialContextFactory")
              (.put Context/PROVIDER_URL jndi-url)
              (.put Context/SECURITY_PRINCIPAL username)
              (.put Context/SECURITY_CREDENTIALS password))]
    (InitialContext. env)))

(defn qconn-factory
  "Create Ems Queue Conn Factory"
  [{:keys [jndi-url connection-factory username password] :as args}]
  (let [ctx (initial-context args)]
    (.lookup ctx connection-factory)))

(defstate ^:dynamic *qcf*
  :start (qconn-factory (env :emsus))
  )

;; ; Send multiple Text messages
(defn send-queue-text-messages [{:keys [username password]} queue-name messages]
  (with-open [connection (-> *qcf*
                             (.createQueueConnection username password))]
    (let [session (.createQueueSession connection false Session/AUTO_ACKNOWLEDGE)
          queue (.createQueue session queue-name)
          sender (.createSender session queue)]
      (.start connection)
      (doseq [item messages]
        (let [message (.createTextMessage session)]
          (log/info "Publishing: " (str "[" item "]"))
          (.setText message item)
          (.send sender message))))))

;; ; Consume Queue Text messages asynchronously
(defn get-queue-text-messages [{:keys [username password]} queue-name process-message]
  (future
    (with-open [connection (-> *qcf*
                               (.createQueueConnection username password))]
      (let [session (.createQueueSession connection false Session/AUTO_ACKNOWLEDGE)
            queue (.createQueue session queue-name)]
        (with-open [receiver (.createReceiver session queue)]
          (.start connection)
          (loop []
            (process-message (.receive receiver))
            (recur)))))))

;; ; Create function aliases with connection information embedded
(defn consume-messages [queue-name message-processor]
  (get-queue-text-messages (env :emsus) queue-name message-processor))

(defn publish-messages [queue-name messages]
  (send-queue-text-messages (env :emsus) queue-name messages))

;; ; Just dump messages to console for now
(defn my-message-processor [message]
  (println (.toString message)))
