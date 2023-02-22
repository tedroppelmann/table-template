(ns re-frame-template.components.sorter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-com.core :refer [button]]))


(defn SortButton [{:keys [column]}]
  (let [sort-by (re-frame/subscribe [::subs/sort-by])
        accessor (:accessor column)]
    (fn []
      (let [value-map (if (accessor @sort-by)
                  (cond
                    (= (-> @sort-by accessor :order) "asc") {:label [:i {:class "zmdi zmdi-sort-amount-asc"}] :next-click "desc"}
                    (= (-> @sort-by accessor :order) "desc") {:label [:i {:class "zmdi zmdi-sort-amount-desc"}] :next-click "delete"})
                  {:label "Sort" :next-click "asc"})]
        [button
         :label  (:label value-map)
         :tooltip "Sort button"
         :on-click (fn [] (re-frame/dispatch [::events/sort accessor (:next-click value-map)]))]))))