(ns re-frame-template.components.filter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [reagent.core :as r]
   [react-number-format :refer (NumericFormat)]))


(defn FilterInput [{:keys [filter-state filter-input-type]}]
  [:input.form-control 
   {:type "text"
    :placeholder (str "Filter by " (-> @filter-state :filter-field-selected :label))
    :value (filter-input-type @filter-state)
    :on-change (fn [e]
                 (swap! filter-state assoc filter-input-type (-> e .-target .-value)))}])

(defn FilterDropdown [{:keys [filter-state]}]
  [:div.dropdown.mt-3
   [:button.btn.dropdown-toggle.w-100
    {:type "button"
     :data-bs-toggle "dropdown"
     :aria-expanded "false"}
    (-> @filter-state :dropdown-selection :name)]
   (into [:ul.dropdown-menu]
         (map
          (fn [filter]
            [:li>a.dropdown-item
             {:type "button"
              :on-click (fn []
                          (swap! filter-state assoc :dropdown-selection filter))}
             (:name filter)])
          (-> @filter-state :filter-options)))])

(defn FilterConfirmation [{:keys [filter-state]}]
  [:div.d-flex.flex-row.justify-content-end.mt-3
   [:button.btn.btn-danger.mr-2
    {:type "button"
     :on-click (fn [] 
                 (re-frame/dispatch [::events/cancel-filter (-> @filter-state :filter-field-selected :accessor)])
                 (swap! filter-state assoc 
                        :filter-input ""
                        :filter-input-max ""
                        :error false))}
    "Cancel"]
   [:button.btn.btn-primary
    {:type "button"
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
                     (swap! filter-state assoc :error false))))}
    "Filter"]])

(defn FilterErrorAlert [{:keys [filter-state]}]
  [:div.alert.alert-danger
   {:role "alert"}
   (-> @filter-state :error)])

(defn FilterFieldSelector [{:keys [filter-state filter-fields filter-options]}]
  [:div.btn-group.dropend.ml-2.align-self-start
   [:button.btn.dropdown-toggle
    {:type "button"
     :data-bs-toggle "dropdown"
     :aria-expanded "false"}]
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
                              :dropdown-selection (first options)})]
    (fn [] 
      [:div.d-flex.flex-column
       (when (-> @filter-state :error)
         [FilterErrorAlert {:filter-state filter-state}])
       [:div.d-flex.flex-row
        [:div.d-flex.flex-column.w-100
         [FilterInput {:filter-state filter-state :filter-input-type :filter-input}]
         [FilterDropdown {:filter-state filter-state}]
         (when (-> @filter-state :dropdown-selection :two-inputs)
           [:div.mt-3
            [FilterInput {:filter-state filter-state :filter-input-type :filter-input-max}]])
         [FilterConfirmation {:filter-state filter-state}]]
        (when (< 1 (count filter-fields))
          [FilterFieldSelector {:filter-state filter-state :filter-fields filter-fields :filter-options filter-options}])]])))
