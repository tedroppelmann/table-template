(ns re-frame-template.components.filter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [reagent.core :as r]
   [re-com.core :refer [v-box h-box input-text single-dropdown button datepicker-dropdown]]))


(defn FilterInput [{:keys [filter-state filter-input-type]}]
  (if (= (-> @filter-state :filter-field-selected :type) "date")
    [datepicker-dropdown
     :model (:date @filter-state) 
     :show-today? true
     :width "100%"
     :start-of-week 0
     :format "dd MMM yyyy"
     :goog? true
     :placeholder (str "Filter by " (-> @filter-state :filter-field-selected :label)) 
     :on-change (fn [e] 
                  (js/console.log e)
                  (swap! filter-state assoc 
                         filter-input-type e
                         :date e))]
    [input-text
     :model (filter-input-type @filter-state)
     :width "100%"
     :placeholder (str "Filter by " (-> @filter-state :filter-field-selected :label))
     :change-on-blur? false
     :on-change (fn [e] (swap! filter-state assoc filter-input-type e))]))

(defn FilterDropdown [{:keys [filter-state]}]
  [single-dropdown
   :model (-> @filter-state :dropdown-selection :key)
   :id-fn :key
   :label-fn :name
   :width "100%"
   :choices (-> @filter-state :filter-options)
   :on-change (fn [e]
                (let [new-selection (first (filter #(= (:key %) e) (-> @filter-state :filter-options)))] ;;Could be better
                  (swap! filter-state assoc :dropdown-selection new-selection)))])

(defn FilterConfirmation [{:keys [filter-state]}]
  [h-box
   :gap "10px"
   :justify :end
   :children [[button
               :label "Cancel"
               :class "btn-danger"
               :on-click (fn []
                           (re-frame/dispatch [::events/cancel-filter (-> @filter-state :filter-field-selected :accessor)])
                           (swap! filter-state assoc
                                  :filter-input ""
                                  :filter-input-max ""
                                  :error false))] 
              [button
               :label "Filter"
               :class "btn-primary"
               :on-click (fn []
                           (if (or (= (-> @filter-state :filter-input) "")
                                   (and (-> @filter-state :dropdown-selection :two-inputs)
                                        (= (-> @filter-state :filter-input-max) "")))
                             (swap! filter-state assoc :error "Empty input")
                             (do
                               (re-frame/dispatch [::events/filter
                                                   (-> @filter-state :filter-field-selected :accessor)
                                                   (-> @filter-state :filter-field-selected :type)
                                                   (-> @filter-state :filter-input)
                                                   (-> @filter-state :dropdown-selection :key)
                                                   (-> @filter-state :filter-input-max)])
                               (swap! filter-state assoc :error false))))]]])

(defn FilterErrorAlert [{:keys [filter-state]}]
  [:div.alert.alert-danger
   {:role "alert"}
   (-> @filter-state :error)])

(defn FilterFieldSelector [{:keys [filter-state filter-fields filter-options]}]
  [:div.btn-group.dropend.ml-2.align-self-start
   [:button.btn.dropdown-toggle
    {:type "button"
     :data-bs-toggle "dropdown"
     :aria-expanded "false"}
    [:i {:class "zmdi zmdi-filter-list"}]]
   (into [:ul.dropdown-menu]
         (map
          (fn [filter-field]
            [:li>a.dropdown-item
             {:type "button"
              :on-click (fn []
                          (let [options (filter #(contains? (set (:types %)) (:type filter-field)) filter-options)]
                            (re-frame/dispatch [::events/cancel-filter (-> @filter-state :filter-field-selected :accessor)])
                            (swap! filter-state assoc 
                                   :filter-input ""
                                   :filter-field-selected filter-field
                                   :filter-options options
                                   :dropdown-selection (first options))))}
             (:label filter-field)])
          filter-fields))])

(defn FilterBox [filter-fields filter-options]
  (let [options (filter #(contains? (set (:types %)) (:type (first filter-fields))) filter-options)
        filter-state (r/atom {:filter-input ""
                              :filter-input-max ""
                              :error false
                              :filter-field-selected (first filter-fields)
                              :filter-options options
                              :dropdown-selection (first options)
                              :dropdown-selection-id (:key (first options))
                              :date nil})]
    (fn [] 
      [v-box
       :children [(when (-> @filter-state :error)
                    [FilterErrorAlert {:filter-state filter-state}])
                  [h-box
                   :style {:width "100%"}
                   :children [[v-box
                               :gap "10px"
                               :style {:flex 1}
                               :children [[FilterInput {:filter-state filter-state :filter-input-type :filter-input}]
                                          [FilterDropdown {:filter-state filter-state}]
                                          (when (-> @filter-state :dropdown-selection :two-inputs)
                                            [FilterInput {:filter-state filter-state :filter-input-type :filter-input-max}])
                                          [FilterConfirmation {:filter-state filter-state}]]]
                              (when (< 1 (count filter-fields))
                                [FilterFieldSelector {:filter-state filter-state :filter-fields filter-fields :filter-options filter-options}])]]]])))
