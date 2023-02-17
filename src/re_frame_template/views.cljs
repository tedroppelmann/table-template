(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]))

(defn Header [{:keys [headers]}]
  (into [:div]
        (map
         (fn [header]
           [:div header])
         headers)))

(defn Footer []
  [:div "Test"])

(defonce columns
  [{:Header (Header {:headers ["Id"]})
    :accessor :id
    }
   {:Header (Header {:headers ["Name" "IBU"]})
    :accessor :name
    :filter-fields [{:label "Name" :accessor :name :type "text"}
                    {:label "IBU" :accessor :ibu :type "number"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:br]
             [:button.btn.btn-primary (:ibu row)]])
    }
   {:Header (Header {:headers ["IBU"]}) 
    :accessor :ibu
    :Footer Footer
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
  [{:name "Equals" :key "equals" :types ["number" "date"]}
   {:name "Contains" :key "contains" :types ["text" "number"]}
   {:name "Between" :key "between" :types ["number" "date"] :two-inputs true}
   {:name "Greater than" :key "greater-than" :types ["number" "date"]}
   {:name "Lower than" :key "lower-than" :types ["number" "date"]}])

(defn main-panel []
  [:div.container
   [:h1.text-center "Beers of the World"]
   [table/Table {:columns columns :filter-options filter-options}]])
