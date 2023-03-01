(ns re-frame-template.components.collapse
  (:require
   [re-com.core :refer [button]]
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]))

(defn CollapseButton [{:keys [row-state row-key]}]
  [button 
   :label (if (:is-expanded? @row-state)
            [:i {:class "zmdi zmdi-caret-down zmdi-hc-2x"}]
            [:i {:class "zmdi zmdi-caret-right zmdi-hc-2x"}])
   :class "btn-link"
   :on-click (fn [] 
               (when (:is-expanded? @row-state)
                 (re-frame/dispatch [::events/delete-row-subcomponent {:row-key row-key}]))
               (swap! row-state assoc :is-expanded? (not (:is-expanded? @row-state))))])

(defn ExtendedComponent [{:keys [row-key row SubComponent]}]
  (re-frame/dispatch [::events/add-row-subcomponent {:row-key row-key 
                                                     :row row}])
  [SubComponent {:row-key row-key}])