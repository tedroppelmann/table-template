(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [reagent.core :as r]))


(defn FilterInput [filter-input key]
  [:input.form-control.mt-3
   {:type "text"
    :placeholder (str "Filter by " (name key))
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

(defn FilterConfirmation [filter-input key type dropdown-selection filter-input-max error]
  [:div.d-flex.flex-row.justify-content-end.mt-3
   [:button.btn.btn-danger.mr-3
    {:type "button"
     :on-click (fn []
                 (re-frame/dispatch [::events/cancel-filter key])
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
                     (re-frame/dispatch [::events/filter key type @filter-input (:key @dropdown-selection) @filter-input-max])
                     (reset! error false))))}
    "Filter"]])

(defn ErrorAlert [error]
  [:div.alert.alert-danger
   {:role "alert"}
   @error])

(defn FilterBox [{:keys [key type]} filter-options]
  (let [filter-input (r/atom "")
        dropdown-selection (r/atom (first filter-options))
        filter-input-max (r/atom "")
        error (r/atom false)]
    (fn []
      [:div.d-flex.flex-column
       (when @error
         [ErrorAlert error])
       [FilterInput filter-input key]
       [FilterDropdown dropdown-selection filter-options]
       (when (:two-inputs @dropdown-selection)
        [FilterInput filter-input-max key])
       [FilterConfirmation filter-input key type dropdown-selection filter-input-max error]])))

(defn SortButton [key]
  (let [order (r/atom true)]
    (fn []
      [:input
       {:type "button"
        :value (str "Sort " @order)
        :on-click (fn []
                    (re-frame/dispatch [::events/sort key])
                    (reset! order (not @order)))}])))

(defn Print []
  [:input
   {:type "button"
    :value "Print filter params"
    :on-click #(re-frame/dispatch [::events/print])}])

(defn Headers [headers filter-options]
  [:thead
   (into [:tr] (map 
                (fn [header] 
                  [:th {:style (:style header)} (:name header)
                   (when (:sorted? header)
                     [SortButton (:key header)])]) 
                headers))
   (into [:tr] (map 
                (fn [header] 
                  [:th
                   (when (:filtered? header)
                     [FilterBox header (filter #(contains? (set (:types %)) (:type header)) filter-options)])]) 
                headers))])

(defn Row [row & params]
  (into [:tr] (map (fn [param] [:td (param row)]) params)))

(defn Table [{:keys [columns filter-options]}]
  (let [elements (re-frame/subscribe [::subs/data])]
    (fn []
      [:div
       [Print]
       [:table.table.table-striped.table-responsive
        [Headers columns filter-options] 
        (when (not @(re-frame/subscribe [::subs/data-loading?]))
          [:tbody
           (for [element @elements]
             ^{:key (:id element)}
             [Row element :id :name :ibu :first_brewed :tagline])])]
       (when @(re-frame/subscribe [::subs/data-loading?])
         [:h3.text-center "Loading..."])])))
