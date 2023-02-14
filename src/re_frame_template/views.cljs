(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]))

(defonce columns
  [{:name "Id" :style {:width "20%"} :key :id :type "number" :sorted? true :filtered? false}
   {:name "Name" :style {:width "30%"} :key :name :type "text" :sorted? true :filtered? true}
   {:name "IBU" :style {:width "10%"} :key :ibu :type "number" :sorted? false :filtered? true}
   {:name "First brewed" :style {:width "10%"} :key :first_brewed :type "date" :sorted? false :filtered? false}
   {:name "Tagline" :style {:width "30%"} :key :tagline :type "text" :sorted? false :filtered? true}])

(defonce filter-options
  [{:name "Equals" :key "equals" :types ["text" "number" "date"]}
   {:name "Contains" :key "contains" :types ["text" "number"]}
   {:name "Between" :key "between" :two-inputs true :types ["number" "date"]}
   {:name "Greater than" :key "greater-than" :types ["number" "date"]}
   {:name "Lower than" :key "lower-than" :types ["number" "date"]}])

(defn main-panel []
  [:div.container
   [:h1.text-center "Beers of the World"]
   [table/Table {:columns columns :filter-options filter-options}]])
