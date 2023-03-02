(ns re-frame-template.views
  (:require 
   [re-frame-template.components.table :as table]
   [react-number-format :refer (NumericFormat)]
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]))

(defn MyHeader [{:keys [headers]}]
  (into [:div]
        (map
         (fn [header]
           [:div header])
         headers)))

(defonce columns2
  [{:Header (MyHeader {:headers ["Seller" "Rating" "IVA"]})
    :accessor :seller_name
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:br]
             (:seller_rating row)
             [:br]
             (:seller_iva row)])}
    
   {:Header (MyHeader {:headers ["Debitore" "Rating" "IVA"]})
    :accessor :debtor_name
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:br]
             (:debtor_rating row)
             [:br]
             (:debtor_iva row)])}
    
   {:Header (MyHeader {:headers ["Email referente" "Nome referente" "Telefono referente"]})
    :accessor :email_referente
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:br]
             (:nome_referente row)
             [:br]
             (:telefono_referente row)])}])

(defonce columns
  [{:Header (MyHeader {:headers ["Id"]})
    :accessor :id}
    
   {:Header (MyHeader {:headers ["Name" "First brewed"]})
    :accessor :name
    :filter-fields [{:label "Name" :accessor :name :type "text"}
                    {:label "First brewed" :accessor :first_brewed :type "date"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:p]
             [:button.btn.btn-primary (:first_brewed row)]])}
    
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
                   [:div (str "IBU Average: " average)]])))}
    
   {:Header (MyHeader {:headers ["Tagline" "Brewers tips"]})
    :accessor :tagline
    :sorted? false
    :filter-fields [{:label "Tagline" :accessor :tagline :type "text"}]
    :Cell (fn [{:keys [row value]}]
            [:div
             value
             [:p]
             (:brewers_tips row)])}])

(defn main-panel []
  [:div.container
   [:h1.text-center "Beers of the World"] 
   [table/Table {:data-key [:beers] 
                 :columns columns
                 :SubComponent (fn [{:keys [row-key row]}]
                                 (re-frame/dispatch [::events/create-beer-data {:data-key [row-key :beers]}])
                                 (re-frame/dispatch [::events/add-row-subcomponent {:data-key [row-key :row] :row row}])
                                 [:<>
                                  [table/Table {:data-key [row-key :row]
                                                :checkable? false
                                                :pagination? false
                                                :columns [{:Header (MyHeader {:headers ["ABV"]})
                                                           :accessor :abv
                                                           :sorted? false}
                                                          {:Header (MyHeader {:headers ["Target FG"]})
                                                           :accessor :target_fg
                                                           :sorted? false}
                                                          {:Header (MyHeader {:headers ["EBC"]})
                                                           :accessor :ebc
                                                           :sorted? false}
                                                          {:Header (MyHeader {:headers ["SRM"]})
                                                           :accessor :srm
                                                           :sorted? false}
                                                          {:Header (MyHeader {:headers ["pH"]})
                                                           :accessor :ph
                                                           :sorted? false}]}]
                                  [table/Table {:data-key [row-key :beers]
                                                :checkable? false
                                                :pagination? false
                                                :columns columns}]])}]])
