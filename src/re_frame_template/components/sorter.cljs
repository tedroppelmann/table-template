(ns re-frame-template.components.sorter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-com.core :refer [button]]))


(defn SortButton [{:keys [column table-key]}]
  (let [sort-by (re-frame/subscribe [::subs/sort-by table-key])
        accessor (:accessor column)]
    (fn []
      (let [value-map (if (accessor @sort-by)
                  (cond
                    (= (-> @sort-by accessor :order) "asc") {:label [:i {:class "zmdi zmdi-sort-amount-asc"}] :next-click "desc" :class "btn-primary"}
                    (= (-> @sort-by accessor :order) "desc") {:label [:i {:class "zmdi zmdi-sort-amount-desc"}] :next-click "delete" :class "btn-primary"})
                  {:label [:i {:class "zmdi zmdi-sort-amount-asc"}] :next-click "asc"})]
        [button
         :label  (:label value-map)
         :class (:class value-map)
         :tooltip "Sort button"
         :on-click (fn [] (re-frame/dispatch [::events/sort {:key accessor 
                                                             :value (:next-click value-map) 
                                                             :table-key table-key}]))]))))