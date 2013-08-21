(ns watson.core
  (:gen-class)
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [clojure.math.combinatorics :as combo]
            [clojure.pprint :refer [pprint]]))

(def settings-file "league_settings.yml")

(def sqlite
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "resources/football.sqlite"})

(defn all-players
  []
  (db/with-connection sqlite
    (db/with-query-results rs
      ["select id, name, team, position, value, adp from players order by adp"]
      (doall rs))))

(defn parse-settings
  []
  (-> settings-file
      io/resource
      slurp
      yaml/parse-string))

(defn determine-pick
  [teams rank round]
  (if (even? round)
    (- (inc teams) rank)
    rank))

(defn find-remaining
  [players teams pick]
  (->> players
       (remove #(= (:id pick) (:id %)))
       (drop (dec teams))))

(defn draft
  [round pick-num players position]
  (let [remaining (drop (dec pick-num) players)]
    (->> remaining
         (filter #(= position (:position %)))
         (sort-by :value)
         last)))

(defn run-draft
  [rank teams players positions]
  (loop [players   players
         positions positions
         round     1
         team      []]
    (if (empty? positions)
      team
      (let [pick-num  (determine-pick teams rank round)
            pick      (draft round pick-num players (first positions))
            remaining (find-remaining players teams pick)]
        (recur remaining
               (rest positions)
               (inc round)
               (conj team pick))))))

(defn draft-value
  [draft]
  (apply + (map :value draft)))

(defn better-draft
  [{:keys [team value] :as a} {:keys [calc-fn requirements] :as b}]
  (let [b-draft (calc-fn requirements)
        b-value (draft-value b-draft)]
    (if (> value b-value)
      a
      {:team  b-draft
       :value b-value})))

(defn append-calc-fn
  [positions calc-fn]
  {:calc-fn calc-fn :requirements positions})

(defn find-best*
  [pick teams players requirements]
  (let [combinations (combo/permutations requirements)
        calc-fn      (partial run-draft pick teams players)
        first-draft  (calc-fn (first combinations))
        first-value  (draft-value first-draft)
        other-drafts (map #(append-calc-fn % calc-fn) (rest combinations))]
    (reduce better-draft
            {:team first-draft
             :value first-value}
            other-drafts)))

(defn find-best
  [pick teams players requirements]
  (if (some #{"FLEX"} requirements)
    (->> [(find-best* pick teams players (replace {"FLEX" "RB"} requirements))
          (find-best* pick teams players (replace {"FLEX" "WR"} requirements))
          (find-best* pick teams players (replace {"FLEX" "TE"} requirements))]
         (sort-by :value)
         last)
    (find-best* pick teams requirements players)))

(defn -main
  [& pick]
  (let [pick (or (Integer/parseInt (first pick)) 1)
        {:keys [requirements teams]} (parse-settings)
        players (all-players)]
    (pprint (find-best pick teams players requirements))))