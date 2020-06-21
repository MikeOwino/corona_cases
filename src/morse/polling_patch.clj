(ns morse.polling-patch
  (:require [clojure.core.async :as a :refer [>! close! go-loop]]
            [morse.api :as api]
            [morse.polling :as p]))

(defn create-producer-with-handle
  "Passed channel should be always empty.
   Close it to stop long-polling.
   Returns channel with updates from Telegram"
  [running token opts api-error-handle-fn]
  (let [updates (a/chan)
        ;; timeout for Telegram API in seconds
        timeout (or (:timeout opts) 1)]
    (go-loop [offset 0]
      (let [;; fix for JDK bug https://bugs.openjdk.java.net/browse/JDK-8075484
            ;; introduce additional timeout 10 times more that telegram's one
            wait-timeout (a/go (a/<! (a/timeout (* 1000 timeout 10)))
                               ::wait-timeout)
            response     (api/get-updates-async token (assoc opts :offset offset))
            [data _] (a/alts! [running response wait-timeout])]
        (case data
          ;; running got closed by the user
          nil
          (do (println "Stopping Telegram polling...")
              (close! wait-timeout)
              (close! updates))

          ::wait-timeout
          (do (println "ERROR" "HTTP request timed out, stopping polling")
              (close! running)
              (close! updates)
              (println "ABORT" "on ::wait-timeout")
              (api-error-handle-fn))

          ::api/error
          (do (println "WARN" "Got error from Telegram API, stopping polling")
              (close! running)
              (close! updates)
              (println "ABORT" "on ::api/error")
              (api-error-handle-fn))

          (do (close! wait-timeout)
              (doseq [upd data] (>! updates upd))
              (recur (p/new-offset data offset))))))
    updates))
