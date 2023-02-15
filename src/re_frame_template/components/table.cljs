(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [reagent.core :as r]))


(defn FilterInput [{:keys [filter-input name]}]
  [:input.form-control.mt-3
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

(defn FilterBox [{:keys [key type name]} filter-options]
  (let [filter-input (r/atom "")
        dropdown-selection (r/atom (first filter-options))
        filter-input-max (r/atom "")
        error (r/atom false)]
    (fn []
      [:div.d-flex.flex-column
       (when @error
         [ErrorAlert error])
       [FilterInput {:filter-input filter-input :name name}]
       [FilterDropdown dropdown-selection filter-options]
       (when (:two-inputs @dropdown-selection)
        [FilterInput {:filter-input filter-input-max :name name}])
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

(defn NestedHeader [{:keys [header filter-options]}]
  (let [fields (:fields header)
        field-selected (r/atom (first fields))]
    (fn []
      [:th {:style (:style header)}
       (into [:div] (map
                     (fn [element]
                       [:div
                        {:type "button"
                         :style {:font-weight (when (not= @field-selected element) "normal")}
                         :on-click (fn [] 
                                     (re-frame/dispatch [::events/cancel-filter (:key @field-selected)])
                                     (reset! field-selected element))}
                        (:name element)])
                     (:fields header)))
       (when (:filtered? @field-selected)
         [FilterBox @field-selected (filter #(contains? (set (:types %)) (:type @field-selected)) filter-options)])])))

(defn NormalHeader [{:keys [header filter-options]}]
  [:th {:style (:style header)}
   (:name header)
   (when (:sorted? header)
     [SortButton (:key header)])
   (when (:filtered? header) 
     [FilterBox header (filter #(contains? (set (:types %)) (:type header)) filter-options)])])

(defn Headers [{:keys [columns filter-options]}]
  [:thead
   (into [:tr] (map 
                (fn [header]
                  (if (:nested? header)
                    [NestedHeader {:header header :filter-options filter-options}]
                    [NormalHeader {:header header :filter-options filter-options}])) 
                columns))])

(defn Row [{:keys [row columns]}]
  (into [:tr] (map
               (fn [column]
                 (if (:nested? column)
                   (into [:td] (map (fn [element] [:div ((:key element) row)]) (:fields column)))
                   [:td ((:key column) row)]))
               columns)))

(defn Table [{:keys [columns filter-options]}]
  (let [data (re-frame/subscribe [::subs/data])
        columns-filtered (filter #(not (:hidden %)) columns)] 
    (fn []
      [:div
       [Print]
       [:table.table.table-striped.table-responsive
        [Headers {:columns columns-filtered :filter-options filter-options}] 
        (when (not @(re-frame/subscribe [::subs/data-loading?]))
          [:tbody
           (for [element @data]
             ^{:key (:id element)}
             [Row {:row element :columns columns-filtered}])])]
       (when @(re-frame/subscribe [::subs/data-loading?])
         [:h3.text-center "Loading..."])])))
