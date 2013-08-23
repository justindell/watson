(ns watson.http
  (:require [org.httpkit.server :refer [run-server]]
            [compojure.handler :refer [site]]
            [compojure.core :refer :all]
            [compojure.route :refer [resources]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [redirect]]
            [watson.db :as db]
            [watson.draft :as draft]
            [watson.view :as view]))

(defn draft
  [id]
  (db/draft id)
  (redirect "/"))

(defn pick
  [id]
  (db/pick id)
  (redirect "/"))

(defroutes build-routes
  (GET  "/"      [undrafted position] (view/players undrafted position))
  (POST "/draft" [id] (draft id))
  (POST "/pick"  [id] (pick id))
  
  (GET  "/api/recommend" [] (draft/recommend))
  
  (resources "/"))

(defn run
  []
  (run-server (wrap-reload (site #'build-routes)) {}))