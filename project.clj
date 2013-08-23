(defproject watson "0.1.0-SNAPSHOT"
  :description "draft the best football team"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.combinatorics "0.0.4"]
                 [clj-yaml "0.4.0"]
                 [org.clojure/java.jdbc "0.0.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [http-kit "2.1.10"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [cheshire "5.2.0"]]
  :main watson.core)
