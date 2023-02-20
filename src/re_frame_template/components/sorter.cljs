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
                    "asc"
                    "desc")
                  "")
            next-click-val (cond
                             (= val "") "asc"
                             (= val "asc") "desc"
                             (= val "desc") "delete")]
        [button
         :label  (str "Sort " val)
         :tooltip "Sort button"
         :on-click (fn [] (re-frame/dispatch [::events/sort (:accessor column) next-click-val]))]))))