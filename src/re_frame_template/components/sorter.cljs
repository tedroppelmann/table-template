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
      (let [val (if (-> @sort-by accessor)
                  (if (= (-> @sort-by accessor :order) "asc")
                    ["asc" [:i {:class "zmdi zmdi-sort-amount-asc"}]]
                    ["desc" [:i {:class "zmdi zmdi-sort-amount-desc"}]])
                  ["" "Sort"])
            next-click-val (cond
                             (= (first val) "") "asc"
                             (= (first val) "asc") "desc"
                             (= (first val) "desc") "delete")]
        [:div {:style {:display "flex" :justify-content "center" :width "100%"}}
         [button
          :label  (last val)
          :tooltip "Sort button"
          :on-click (fn [] (re-frame/dispatch [::events/sort (:accessor column) next-click-val]))]]))))