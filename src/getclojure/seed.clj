(ns getclojure.seed
  (:require [clojure.java.io :as io]
            [clojurewerkz.elastisch.rest :refer [connect!]]
            [clojurewerkz.elastisch.rest.index :refer [exists? create delete]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [getclojure.search :refer [create-getclojure-index add-to-index]]
            [getclojure.models.user :refer [create-user!]]
            [getclojure.models.sexp :refer [create-sexp!]]))

(def sexps
  (->> (io/file "working-sexps.db")
       slurp
       read-string
       (into #{})))

(defn seed-sexps [sexp-set]
  (let [numbered-sexps (sort-by key (zipmap (iterate inc 1) sexp-set))
        cnt (count numbered-sexps)
        user (create-user! "admin@getclojure.org" "admin")]
    (doseq [[n sexp] numbered-sexps]
      (println (str n "/" cnt))
      (println (:input sexp))
      (if-not (mc/any? "sexps" {:raw-input (:input sexp)})
        (let [id (:id (create-sexp! user sexp))]
          (add-to-index :getclojure (assoc sexp :id id)))))))

(defn add-sexps-to-index [sexp-set]
  (let [numbered-sexps (sort-by key (zipmap (iterate inc 1) sexp-set))
        cnt (count numbered-sexps)]
    (doseq [[n sexp] numbered-sexps]
      (println (str n "/" cnt))
      (add-to-index :getclojure sexp))))

(defn -main []
  (println "Attempting to connect to elastic search...")
  (let [search-endpoint (or (System/getenv "BONSAI_URL")
                            "http://127.0.0.1:9200")
        mongo-uri (or (System/getenv "MONGOLAB_URI")
                      "mongodb://127.0.0.1/getclojure_development")
        mongo-uri "mongodb://heroku_app11300183:5i1uhb3oojqo6da8qe829f58c0@ds029297.mongolab.com:29297/heroku_app11300183"]
    (println "The elastic search endpoint is" search-endpoint)

    (println "Connecting to MongoDB:" mongo-uri)
    (if (= "development" (if (.contains mongo-uri "heroku")
                           "production"
                           "development"))
      (do (println "Deleting all users")
          (mc/remove :users)
          (println "Deleting all existing sexps")
          (mc/remove :sexps)))

    (println "Connecting to" search-endpoint)
    (connect! search-endpoint)

    (let [idx-name "getclojure"]
      (if (exists? idx-name)
        (do (println "The index" idx-name "already existed!")
            (println "Deleting" idx-name "...")
            (delete idx-name))
        (println "The" idx-name "index doesn't exist..."))
      (println "Creating" idx-name "index...")
      (create-getclojure-index))
    (println "Populating the index...")
    (time (seed-sexps sexps))))