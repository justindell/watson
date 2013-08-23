(ns watson.core
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [watson.http :as http]))

(defn -main
  [& draft-pick]
  (http/run))