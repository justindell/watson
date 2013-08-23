(ns watson.draft
  (:require [clojure.math.combinatorics :as combo]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [watson.db :as db]))

(def settings-file "league_settings.yml")

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
  [players teams {:keys [id]}]
  (->> players
       (remove #(= id (:id %)))
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
  [{:keys [team value] :as a} {:keys [calc-fn requirements]}]
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

(defn remove-first [x coll]
  (lazy-seq
   (when (seq coll)
     (let [[y & ys] coll]
       (if (= x y) 
         ys  
         (cons y (remove-first x ys)))))))

(defn recommend
  []
  (let [{:keys [requirements teams pick]} (parse-settings)
        mine         (map :position (db/all ["mine = '1'"]))
        undrafted    (db/all ["drafted = 'f'"])
        requirements (reduce (fn [acc n] (remove-first n acc)) requirements mine)]
    (println "pick:" pick "requirements:" requirements)
    (->  (find-best pick teams undrafted requirements)
         :team
         json/encode)))

;; TODO where to use this
(defn best-team
  []
  (let [{:keys [requirements teams pick]} (parse-settings)
        players (db/all)]
    (find-best pick teams players requirements)))