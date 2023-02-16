(ns re-frame-template.components.filter
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [reagent.core :as r]))

(defn FilterInput [{:keys [filter-input name]}]
  [:input.form-control
   {:type "text"
    :placeholder (str "Filter by " name)
    :value @filter-input
    :on-change #(reset! filter-input (-> % .-target .-value))}])

(defn FilterDropdown [dropdown-selection filter-options]
  [:div.dropdown.mt-3
   [:button.btn.dropdown-toggle.w-100
    {:type "button"
     :data-bs-toggle "dropdown"
     :aria-expanded "false"}
    (:name @dropdown-selection)]
   (into [:ul.dropdown-menu]
         (map
          (fn [filter]
            [:li>a.dropdown-item
             {:type "button"
              :on-click #(reset! dropdown-selection filter)}
             (:name filter)])
          filter-options))])

(defn FilterConfirmation [filter-input accessor type dropdown-selection filter-input-max error]
  [:div.d-flex.flex-row.justify-content-end.mt-3
   [:button.btn.btn-danger.mr-2
    {:type "button"
     :on-click (fn []
                 (re-frame/dispatch [::events/cancel-filter accessor])
                 (reset! filter-input "")
                 (reset! filter-input-max "")
                 (reset! error false))}
    "Cancel"]
   [:button.btn.btn-primary
    {:type "button"
     :on-click (fn []
                 (if (or (= @filter-input "") (and (:two-inputs @dropdown-selection) (= @filter-input-max "")))
                   (reset! error "Empty input")
                   (do
                     (re-frame/dispatch [::events/filter accessor type @filter-input (:key @dropdown-selection) @filter-input-max])
                     (reset! error false))))}
    "Filter"]])

(defn FilterErrorAlert [error]
  [:div.alert.alert-danger
   {:role "alert"}
   @error])

(defn FilterFieldSelector [{:keys [filter-field-selected filter-fields filter-input dropdown-options filter-options dropdown-selection]}]
  [:div.btn-group.dropend.ml-2
   [:button.btn.dropdown-toggle
    {:type "button"
     :data-bs-toggle "dropdown"
     :aria-expanded "false"}]
   (into [:ul.dropdown-menu]
         (map
          (fn [filter]
            [:li>a.dropdown-item
             {:type "button"
              :on-click (fn []
                          (let [fil (filter #(contains? (set (:types %)) (:type filter)) filter-options)]
                            (re-frame/dispatch [::events/cancel-filter (:accessor @filter-field-selected)])
                            (reset! filter-field-selected filter)
                            (reset! dropdown-selection (first fil))
                            (reset! filter-input "")))}
             (:label filter)])
          filter-fields))])

(defn FilterBox [filter-fields filter-options]
  (let [filter-state (r/atom {:filter-input ""
                              :filter-input-max ""
                              :error false
                              :filter-field-selected (first filter-fields)
                              :dropdown-selection (first (filter #(contains? (set (:types %)) (:type (first filter-fields))) filter-options))})
        filter-input (r/atom "")
        filter-input-max (r/atom "")
        error (r/atom false)
        filter-field-selected (r/atom (first filter-fields))
        dropdown-options (r/atom (filter #(contains? (set (:types %)) (:type @filter-field-selected)) filter-options))
        dropdown-selection (r/atom (first @dropdown-options))]
    (fn []
      [:div.d-flex.flex-column
       (when @error
         [FilterErrorAlert error])
       [:div.d-flex.flex-row
        [FilterInput {:filter-input filter-input
                      :name (:label @filter-field-selected)}]
        [FilterFieldSelector {:filter-field-selected filter-field-selected
                              :filter-fields filter-fields
                              :filter-input filter-input
                              :dropdown-options dropdown-options
                              :dropdown-selection dropdown-selection
                              :filter-options filter-options}]]
       [FilterDropdown dropdown-selection (filter #(contains? (set (:types %)) (:type @filter-field-selected)) filter-options)]
       (when (:two-inputs @dropdown-selection)
         [:div.mt-3
          [FilterInput {:filter-input filter-input-max :name (:label @filter-field-selected)}]])
       [FilterConfirmation filter-input (:accessor @filter-field-selected) (:type @filter-field-selected) dropdown-selection filter-input-max error]])))
