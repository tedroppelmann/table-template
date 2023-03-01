(ns re-frame-template.components.checkbox
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-com.core :refer [checkbox h-box button]]))

(defn CheckBox [{:keys [row table-key]}] ;; I need to know the key of the row, maybe not always is id
  (let [key (keyword (str (:id row)))
        checked-map (re-frame/subscribe [::subs/checked-map table-key])] 
    (fn [] 
      [checkbox 
       :model (key @checked-map) 
       :on-change (fn [e] (re-frame/dispatch [::events/change-checked-map {:row row 
                                                                           :bool e 
                                                                           :key key 
                                                                           :table-key table-key}]))])))

(defn CheckAll [{:keys [table-key]}]
  (let [check-all? (re-frame/subscribe [::subs/check-all? table-key])]
    (fn []
      [checkbox
       :model @check-all?
       :on-change (fn [e] (re-frame/dispatch [::events/check-all {:bool e 
                                                                  :table-key table-key}]))])))

(defn CheckOptions [{:keys [table-key]}]
  (let [checked-map (re-frame/subscribe [::subs/checked-map table-key])]
    [h-box
     :gap "10px"
     :children [[button
                 :label [:i {:class "zmdi zmdi-copy"}]
                 :disabled? (when (empty? @checked-map) true)]
                [button
                 :label [:i {:class "zmdi zmdi-download"}] 
                 :disabled? (when (empty? @checked-map) true)]
                [button
                 :label [:i {:class "zmdi zmdi-delete"}]
                 :class "btn-danger"
                 :disabled? (when (empty? @checked-map) true)]]]))