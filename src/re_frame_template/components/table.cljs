(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [reagent.core :as r]
   [re-frame-template.components.filter :as filter]
   [re-frame-template.components.sorter :as sorter]
   [re-frame-template.components.pagination :as pagination]
   [re-frame-template.components.checkbox :as checkbox]
   [re-frame-template.components.collapse :as collapse]
   [re-com.core :refer [throbber h-box box v-box]]))


(defn Row [{:keys [row columns checkable? SubComponent table-key]}]
  (let [row-state (r/atom {:is-expanded? false})
        colspan (+ (count columns) (if checkable? 2 1))
        row-key (keyword (str (random-uuid)))]
    (fn []
      [:<>
       (into [:tr
              (when SubComponent [:td [collapse/CollapseButton {:row-state row-state :row-key row-key}]])
              (when checkable? [:td [checkbox/CheckBox {:row row :table-key table-key}]])]
             (map
              (fn [column]
                [:td
                 (if (:Cell column)
                   [(:Cell column) {:row row :value ((:accessor column) row)}]
                   ((:accessor column) row))])
              columns))
       (when (:is-expanded? @row-state)
         [:tr [:td {:colSpan colspan} [collapse/ExtendedComponent {:row-key row-key :row row :SubComponent SubComponent}]]])])))

(defn Footer [{:keys [columns data checkable? SubComponent]}]
  [:tfoot
   (into [:tr
          (when SubComponent [:td])
          (when checkable? [:td])]
         (map
          (fn [column]
            [:td
             (when (:Footer column)
               [(:Footer column) {:column column :data data}])])
          columns))])

(defn Header [{:keys [columns checkable? SubComponent table-key]}]
  [:thead
   (into [:tr
          (when SubComponent [:th {:style {:width "50px"}}])
          (when checkable? [:th {:style {:width "150px"}} [checkbox/CheckAll {:table-key table-key}]])]
         (map
          (fn [column]
            (let [{:keys [sorted?] :or {sorted? true}} column]
              [:th
               [h-box
                :justify :between
                :children [[box
                            :align-self :center
                            :child (:Header column)]
                           (when sorted?
                             [box
                              :align-self :end
                              :child [sorter/SortButton {:column column :table-key table-key}]])]]]))
          columns))
   (when (or checkable? (some #(:filter-fields %) columns))
     (into [:tr
            (when SubComponent [:th])
            (when checkable? [:th [checkbox/CheckOptions {:table-key table-key}]])]
           (map
            (fn [column]
              [:th
               (when (:filter-fields column)
                 [filter/FilterBox {:filter-fields (:filter-fields column) :table-key table-key}])])
            columns)))])

(defn Body [{:keys [table-key columns checkable? SubComponent]}]
  (let [data (re-frame/subscribe [::subs/data table-key])
        loading? (re-frame/subscribe [::subs/data-loading? table-key])]
    (fn []
      [:<>
       (when (not @loading?)
         (into [:tbody]
               (map
                (fn [row]
                  [Row {:row row
                        :columns columns
                        :checkable? checkable?
                        :SubComponent SubComponent
                        :table-key table-key}])
                @data)))
       (when (and (not @loading?) (seq @data))
         [Footer {:columns columns :data @data :checkable? checkable? :SubComponent SubComponent}])])))

(defn WaitingComponent [{:keys [table-key]}]
  (let [data (re-frame/subscribe [::subs/data table-key])
        loading? (re-frame/subscribe [::subs/data-loading? table-key])]
    (fn []
      (if (or @loading? (nil? @loading?))
        [:div
         {:style {:display "flex" :justify-content "center"}}
         [throbber :size :large]]
        (when (empty? @data)
          [:h3.text-center "No data"])))))

(defn Table [{:keys [data-key columns checkable? SubComponent pagination?] :or {checkable? true pagination? true}}]
  (let [table-key data-key
        columns-filtered (filter #(not (:hidden? %)) columns)]
    (fn []
      (js/console.log (str "RENDER TABLE " table-key))
      (re-frame/dispatch [::events/create-new-table {:table-key table-key}])
      [:div
       [v-box
        :children [(when pagination?
                     [box
                      :align-self :end
                      :child [pagination/Pagination {:table-key table-key}]])
                   [:table.table {:style {:table-layout "fixed" :width "100%"}}
                    [Header {:columns columns-filtered
                             :checkable? checkable?
                             :SubComponent SubComponent
                             :table-key table-key}]
                    [Body {:columns columns-filtered
                           :checkable? checkable?
                           :SubComponent SubComponent
                           :table-key table-key}]]
                   [WaitingComponent {:table-key table-key}]]]])))