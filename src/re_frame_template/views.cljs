(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]
   [react-number-format :refer (NumericFormat)]))

(defn Header [{:keys [headers]}]
  (into [:div]
        (map
         (fn [header]
           [:div header])
         headers)))

(defonce columns
  [{:Header (Header {:headers ["Id"]})
    :accessor :id
    }
   {:Header (Header {:headers ["Name" "PH"]})
    :accessor :name
    :filter-fields [{:label "Name" :accessor :name :type "text"}
                    {:label "PH" :accessor :ph :type "number"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:br]
             [:button.btn.btn-primary (:ph row)]])
    }
   {:Header (Header {:headers ["IBU"]}) 
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
   {:Header (Header {:headers ["Tagline" "Brewers tips"]})
    :accessor :tagline
    :not-sorted? true
    :filter-fields [{:label "Tagline" :accessor :tagline :type "text"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:p]
             (:brewers_tips row)])
    }])

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
