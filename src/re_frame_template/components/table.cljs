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


(defn Print []
  [:div
   [:input
    {:type "button"
     :value "Print filter params"
     :on-click #(re-frame/dispatch [::events/print])}]
   (let [query-map (re-frame/subscribe [::subs/query-map])] 
     [:p (str @query-map)])])

(defn Row [{:keys [row columns checkable? SubComponent colspan]}]
  (let [row-state (r/atom {:is-expanded? false})] 
    (fn []
      [:<>
       (into [:tr
              (when SubComponent [:td [collapse/CollapseButton {:row-state row-state}]])
              (when checkable? [:td [checkbox/CheckBox {:row row}]])]
             (map
              (fn [column]
                [:td
                 (if (:Cell column)
                   [(:Cell column) {:row row :value ((:accessor column) row)}]
                   ((:accessor column) row))])
              columns))
       (when (:is-expanded? @row-state)
         [:tr [:td {:colSpan colspan} [SubComponent {:row row}]]])])))

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

(defn Header [{:keys [columns checkable? :SubComponent]}] 
  [:thead
   (into [:tr 
          (when SubComponent [:th {:style {:width "50px"}}])
          (when checkable? [:th {:style {:width "150px"}} [checkbox/CheckAll]])] 
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
                              :child [sorter/SortButton {:column column}]])]]]))
          columns))
   (into [:tr 
          (when SubComponent [:th])
          (when checkable? [:th [checkbox/CheckOptions]])]
         (map
          (fn [column]
            [:th
             (when (:filter-fields column)
               [filter/FilterBox (:filter-fields column)])])
          columns))])


(defn Table [{:keys [data columns checkable? SubComponent] :or {checkable? true}}]
  (let [columns-filtered (filter #(not (:hidden? %)) columns)
        loading? (re-frame/subscribe [::subs/data-loading?])
        colspan (+ (count columns) (if checkable? 2 1))] 
    (fn []
      (js/console.log "RENDER TABLE")
      (js/console.log (random-uuid))
      [:div
       [Print]
       [v-box
        :children [[box
                    :align-self :end
                    :child [pagination/Pagination {:data @data}]]
                   [:table.table {:style {:table-layout "fixed" :width "100%"}}
                    [Header {:columns columns-filtered :checkable? checkable? :SubComponent SubComponent}]
                    (when (not @loading?)
                      (into [:tbody]
                            (map
                             (fn [row] 
                               [Row {:row row :columns columns-filtered :checkable? checkable? :SubComponent SubComponent :colspan colspan}])
                             @data)))
                    (when (and (not @loading?) (seq @data))
                      [Footer {:columns columns-filtered :data @data :checkable? checkable? :SubComponent SubComponent}])]
                   (if @loading?
                     [:div
                      {:style {:display "flex" :justify-content "center"}}
                      [throbber :size :large]]
                     (when (empty? @data)
                       [:h3.text-center "No data"]))]]])))
