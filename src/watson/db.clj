(ns watson.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]))

(def sqlite
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "resources/football.sqlite"})

(def default-select
  "select id, name, team, bye, position, value, drafted, mine, adp from players ")

(defn all
  [where-clauses]
  (let [where (when-not (empty? where-clauses)
                (str "where " (string/join " and " (remove string/blank? where-clauses))))
        order " order by adp"]
    (jdbc/with-connection sqlite
      (jdbc/with-query-results rs
        [(str default-select where order)]
        (doall rs)))))

(defn draft
  [player-id]
  (jdbc/with-connection sqlite
    (jdbc/update-values :players ["id = ?" player-id] {:drafted true})))

(defn pick
  [player-id]
  (jdbc/with-connection sqlite
    (jdbc/update-values :players ["id = ?" player-id] {:mine true :drafted true})))

