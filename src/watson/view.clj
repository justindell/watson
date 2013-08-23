(ns watson.view
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js]]
            [hiccup.form :as form]
            [watson.db :as db]))

(defn search-params
  [undrafted position]
  (let [undrafted-part (when undrafted "drafted = 'f'")
        position-part  (when-not (empty? position) (str "position = '" position "'"))]
    (filter identity [undrafted-part position-part])))

(defn draft-form
  [location {:keys [id]}]
  (form/form-to [:post (str "/" location)]
                (form/hidden-field :id id)
                (form/submit-button {:class "btn btn-primary btn-xs"} location)))

(defn player-class
  [player]
  (let [c (if (= 1 (:drafted player)) "active" "")]
    (str c " " (condp = (:position player)
                 "QB" "text-danger"
                 "RB" "text-warning"
                 "WR" "text-info"
                 "TE" "text-success"
                 ""))))

(defn players
  [undrafted position]
  (html
   [:head
    (include-css "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")
    (include-js "//code.jquery.com/jquery-1.10.1.min.js")
    (include-js "//tablesorter.com/__jquery.tablesorter.min.js")
    (include-js "//jnathanson.com/blog/client/jquery/heatcolor/jquery.heatcolor.0.0.1.pack.js")
    (include-js "application.js")]
   [:body
    [:div.col-md-8
     [:table.table.table-condensed.tablesorter {:id "player-table"}
      [:thead
       [:tr
        [:th "Player"]
        [:th "Team"]
        [:th "Bye"]
        [:th "Position"]
        [:th "Value"]
        [:th "ADP"]
        [:th]
        [:th]]]
      [:tbody
       (for [player (db/all (search-params undrafted position))]
         [:tr {:class (player-class player)}
          [:td (:name player)]
          [:td (:team player)]
          [:td (:bye player)]
          [:td (:position player)]
          [:td (format "%.2f" (bigdec (:value player)))]
          [:td (:adp player)]
          [:td (draft-form "draft" player)]
          [:td (draft-form "pick" player)]])]]]
    [:div.col-md-4
     [:br]
     [:button.btn.btn-info.btn-block {:id "colorize"} "Colorize"]
     [:h4 "Search"]
     (form/form-to {:role "form"}
                   [:get "/"]
                   [:div.form-group
                    (form/text-field {:class "input-small" :placeholder "position"} :position position)]
                   [:div.checkbox
                    [:label
                     (form/check-box :undrafted undrafted)
                     "undrafted"]]
                   (form/submit-button {:class "btn btn-primary"} "Search"))
     [:br]
     [:h4 "Team"]
     [:ul
      (for [player (db/all ["mine = '1'"])]
        [:li (str (:position player) " " (:name player) " (" (:team player) ")")])]
     [:br]
     [:h4 "Taken"]
     (let [drafted (db/all ["drafted = '1'"])]
       [:table
        [:tr [:td "QB"] [:td (count (filter #(= "QB" (:position %)) drafted))]]
        [:tr [:td "RB"] [:td (count (filter #(= "RB" (:position %)) drafted))]]
        [:tr [:td "WR"] [:td (count (filter #(= "WR" (:position %)) drafted))]]
        [:tr [:td "TE"] [:td (count (filter #(= "TE" (:position %)) drafted))]]
        [:tr [:td "K"] [:td (count (filter #(= "K" (:position %)) drafted))]]])
     [:br]
     [:h4.pull-left "Recommend &nbsp;"]
     [:button.btn.btn-success {:id "go"} "Go"]
     [:div.clearfix]
     [:ul.list-unstyled {:id "recommended"}]]]))