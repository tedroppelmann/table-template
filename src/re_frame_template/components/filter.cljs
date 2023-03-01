(ns re-frame-template.components.filter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [reagent.core :as r]
   [re-com.core :refer [v-box h-box input-text single-dropdown button datepicker-dropdown]]
   [cljs-time.coerce :as cljs-time]))

(defonce filter-options
  [{:name "Equals" :key "equals" :types ["text" "number" "date"]}
   {:name "Contains" :key "contains" :types ["text" "number"]}
   {:name "Between" :key "between" :types ["number" "date"] :two-inputs true}
   {:name "Greater than" :key "greater-than" :types ["number" "date"]}
   {:name "Lower than" :key "lower-than" :types ["number" "date"]}])

(defn FilterInput [{:keys [filter-state filter-input-type-key]}]
  (let [field-type (-> @filter-state :filter-field-selected :type)]
    (case field-type
      "date" [datepicker-dropdown
              :model (-> @filter-state :date filter-input-type-key)
              :show-today? true
              :width "100%"
              :start-of-week 0
              :format "dd MMM yyyy"
              :placeholder (str "Filter by " (-> @filter-state :filter-field-selected :label))
              :on-change (fn [e]
                           (swap! filter-state assoc filter-input-type-key (cljs-time/to-string e))
                           (swap! filter-state assoc-in [:date filter-input-type-key] e))]
      [input-text
       :model (filter-input-type-key @filter-state)
       :width "100%"
       :placeholder (str "Filter by " (-> @filter-state :filter-field-selected :label))
       :change-on-blur? false
       :on-change (fn [e] (swap! filter-state assoc filter-input-type-key e))])))

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

(defn FilterConfirmation [{:keys [filter-state table-key]}]
  [h-box
   :gap "10px"
   :justify :end
   :children [[button
               :label "Cancel"
               :class "btn-danger"
               :on-click (fn []
                           (re-frame/dispatch [::events/cancel-filter (-> @filter-state :filter-field-selected :accessor) table-key])
                           (swap! filter-state assoc
                                  :filter-input ""
                                  :filter-input-max ""
                                  :date {}))] 
              [button
               :label "Filter"
               :class "btn-primary"
               :disabled? (if (or (= (-> @filter-state :filter-input) "") 
                                  (and (-> @filter-state :dropdown-selection :two-inputs) 
                                       (= (-> @filter-state :filter-input-max) "")))
                           true
                           false)
               :on-click (fn [] (re-frame/dispatch [::events/filter @filter-state table-key]))]]])

(defn FilterFieldSelector [{:keys [filter-state filter-fields]}]
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
                          (let [options (filter #(contains? (set (:types %)) (:type filter-field)) filter-options)
                                previous-filter-field-key (-> @filter-state :filter-field-selected :accessor)]
                            (swap! filter-state assoc 
                                   :filter-input ""
                                   :filter-input-max "" 
                                   :filter-field-selected filter-field
                                   :filter-options options
                                   :dropdown-selection (first options)
                                   :previous-filter-field-accessor previous-filter-field-key
                                   :date {})))}
             (:label filter-field)])
          filter-fields))])

(defn FilterBox [{:keys [filter-fields table-key]}]
  (let [options (filter #(contains? (set (:types %)) (:type (first filter-fields))) filter-options)
        filter-state (r/atom {:filter-input ""
                              :filter-input-max ""
                              :filter-field-selected (first filter-fields)
                              :filter-options options
                              :dropdown-selection (first options)
                              :dropdown-selection-id (:key (first options)) 
                              :date {}
                              :previous-filter-field-accessor false})]
    (fn [] 
      [v-box
       :children [[h-box
                   :style {:width "100%"}
                   :children [[v-box
                               :gap "10px"
                               :style {:flex 1}
                               :children [[FilterInput {:filter-state filter-state 
                                                        :filter-input-type-key :filter-input}]
                                          [FilterDropdown {:filter-state filter-state}]
                                          (when (-> @filter-state :dropdown-selection :two-inputs)
                                            [FilterInput {:filter-state filter-state 
                                                          :filter-input-type-key :filter-input-max}])
                                          [FilterConfirmation {:filter-state filter-state 
                                                               :table-key table-key}]]]
                              (when (< 1 (count filter-fields))
                                [FilterFieldSelector {:filter-state filter-state 
                                                      :filter-fields filter-fields}])]]]])))
