(ns re-frame-template.components.checkbox
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-com.core :refer [checkbox h-box button]]))

(defn CheckBox [{:keys [row]}] ;; I need to know the key of the row, maybe not always is id
  (let [key (keyword (str (:id row)))
        checked-map (re-frame/subscribe [::subs/checked-map])] 
    (fn [] 
      [checkbox 
       :model (key @checked-map) 
       :on-change (fn [e]
                    (re-frame/dispatch [::events/change-checked-map row e key]))])))

(defn CheckAll []
  (let [check-all? (re-frame/subscribe [::subs/check-all?])]
    (fn []
      [checkbox
       :model @check-all?
       :on-change (fn [e]
                    (re-frame/dispatch [::events/check-all e]))])))

(defn CheckOptions []
  (let [checked-map (re-frame/subscribe [::subs/checked-map])]
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