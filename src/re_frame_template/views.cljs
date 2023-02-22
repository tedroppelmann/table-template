(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]
   [react-number-format :refer (NumericFormat)]))

(defn MyHeader [{:keys [headers]}]
  (into [:div]
        (map
         (fn [header]
           [:div header])
         headers)))

(defonce columns
  [{:Header (MyHeader {:headers ["Id"]})
    :accessor :id
    }
   {:Header (MyHeader {:headers ["Name" "First brewed" "ABV"]})
    :accessor :name
    :filter-fields [{:label "Name" :accessor :name :type "text"}
                    {:label "First brewed" :accessor :first_brewed :type "date"}
                    {:label "ABV" :accessor :abv :type "number"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:p]
             [:button.btn.btn-primary (:first_brewed row)]
             [:p]
             (:abv row)])
    }
   {:Header (MyHeader {:headers ["IBU"]}) 
    :accessor :ibu
    :filter-fields [{:label "IBU" :accessor :ibu :type "number"}] 
    :Cell (fn [{:keys [row value]}]
            [:div 
             [:> NumericFormat {:value value
                                :prefix "IBU "
                                :displayType "text"
                                :decimalSeparator ","}]])
    :Footer (fn [{:keys [data column]}]
              (let [numerator (reduce (fn [k v] (+ k ((:accessor column) v))) 0 data)
                    denominator (count data)
                    average (/ numerator denominator)]
                (fn []
                  [:div
                   [:div (str "IBU Average: " average)]])))
    }
   {:Header (MyHeader {:headers ["Tagline" "Brewers tips"]})
    :accessor :tagline
    :sorted? false
    :filter-fields [{:label "Tagline" :accessor :tagline :type "text"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:p]
             (:brewers_tips row)])
    }])

(defn main-panel []
  [:div.container
   [:h1.text-center "Beers of the World"] 
   [table/Table {:columns columns
                 ;; :checkable? false
                 }]])
