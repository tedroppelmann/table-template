(ns re-frame-template.components.collapse
  (:require
   [re-com.core :refer [button]]))

(defn CollapseButton [{:keys [row-state]}]
  [button 
   :label (if (:is-expanded? @row-state)
            [:i {:class "zmdi zmdi-caret-down zmdi-hc-2x"}]
            [:i {:class "zmdi zmdi-caret-right zmdi-hc-2x"}])
   :class "btn-link"
   :on-click (fn [] (swap! row-state assoc :is-expanded? (not (:is-expanded? @row-state))))])
