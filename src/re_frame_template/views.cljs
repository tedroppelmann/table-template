(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]))

(defonce columns
  [{:name "Id" :style {:width "15%"} :key :id :type "number" :sorted? false :filtered? false}
   {:name "Name" :style {:width "30%"} :key :name :type "text" :sorted? false :filtered? true}
   {:name "IBU" :style {:width "10%"} :key :ibu :type "number" :sorted? false :filtered? true}
   {:name "First brewed" :style {:width "10%"} :key :first_brewed :type "date" :sorted? false :filtered? false :hidden true}
   {:name "Tagline" :style {:width "30%"} :key :tagline :type "text" :sorted? false :filtered? false}
   {:name "Data" :style {:width "30%"} :key :data :type "text" :sorted? false :filtered? false :nested? true 
    :fields [{:name "Name" :style {} :key :name :type "text" :sorted? false :filtered? true}
             {:name "IBU" :style {} :key :ibu :type "number" :sorted? false :filtered? true}
             {:name "First brewed" :style {} :key :first_brewed :type "date" :sorted? false :filtered? false}]}])

(defonce filter-options
  [{:name "Equals" :key "equals" :types ["text" "number" "date"]}
   {:name "Contains" :key "contains" :types ["text" "number"]}
   {:name "Between" :key "between" :types ["number" "date"] :two-inputs true}
   {:name "Greater than" :key "greater-than" :types ["number" "date"]}
   {:name "Lower than" :key "lower-than" :types ["number" "date"]}])

(defn main-panel []
  [:div.container
   [:h1.text-center "Beers of the World"]
   [table/Table {:columns columns :filter-options filter-options}]])
