(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-frame-template.components.filter :as filter]
   [re-frame-template.components.sorter :as sorter]
   [re-frame-template.components.pagination :as pagination]
   [re-frame-template.components.checkbox :as checkbox]
   [re-com.core :refer [throbber h-box box v-box]]))


(defn Print []
  [:div
   [:input
    {:type "button"
     :value "Print filter params"
     :on-click #(re-frame/dispatch [::events/print])}]
   (let [query-map (re-frame/subscribe [::subs/query-map])] 
     [:p (str @query-map)])])

(defn Row [{:keys [row columns checkable?]}]
  (into [:tr (when checkable? [:td [checkbox/CheckBox {:row row}]])] 
        (map
         (fn [column]
           [:td
            (if (:Cell column)
              [(:Cell column) {:row row :value ((:accessor column) row)}]
              ((:accessor column) row))])
         columns)))

(defn Footer [{:keys [columns data checkable?]}]
  [:tfoot
   (into [:tr (when checkable? [:td])] 
         (map
          (fn [column]
            [:td
             (when (:Footer column)
               [(:Footer column) {:column column :data data}])])
          columns))])

(defn Header [{:keys [columns checkable?]}] 
  [:thead
   (into [:tr (when checkable? [:th [checkbox/CheckAll]])] 
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
   (into [:tr (when checkable? [:th [checkbox/CheckOptions]])]
         (map
          (fn [column]
            [:th
             (when (:filter-fields column)
               [filter/FilterBox (:filter-fields column)])])
          columns))])


(defn Table [{:keys [columns checkable?] :or {checkable? true}}]
  (let [data (re-frame/subscribe [::subs/data])
        columns-filtered (filter #(not (:hidden? %)) columns)
        loading? (re-frame/subscribe [::subs/data-loading?])
        ] 
    (fn []
      (js/console.log "RENDER TABLE")
      [:div
       [Print]
       [v-box
        :children [[box
                    :align-self :end
                    :child [pagination/Pagination {:data @data}]]
                   [:table.table.table-striped {:style {:table-layout "fixed" :width "100%"}}
                    [Header {:columns columns-filtered :checkable? checkable?}]
                    (when (not @loading?)
                      (into [:tbody]
                            (map
                             (fn [row]
                               [Row {:row row :columns columns-filtered :checkable? checkable?}])
                             @data)))
                    (when (and (not @loading?) (seq @data))
                      [Footer {:columns columns-filtered :data @data :checkable? checkable?}])]
                   (if @loading?
                     [:div
                      {:style {:display "flex" :justify-content "center"}}
                      [throbber :size :large]]
                     (when (empty? @data)
                       [:h3.text-center "No data"]))]]])))
