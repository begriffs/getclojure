(ns getclojure.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defn get-db-uri []
  (get (System/getenv)
       "MONGOLAB_URI"
       "mongodb://127.0.0.1/getclojure_development"))

(defn make-connection! []
  (let [db-uri (get-db-uri)
        environment (if (.contains db-uri "heroku") "production" "development")]
    (mg/connect-via-uri! db-uri)
    (if (.contains db-uri "heroku")
      (mg/use-db! "getclojure")
      (mg/use-db! "getclojure_development"))
    (mc/ensure-index "sexps" {:user 1})
    (mc/ensure-index "sexps" {:id 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-output 1})
    (mc/ensure-index "sexps" {:raw-value 1})
    {:environment environment}))