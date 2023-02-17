(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [re-frame-template.components.filter :as filter]
   [re-frame-template.components.sorter :as sorter]
   [re-frame-template.components.pagination :as pagination]))


(defn Print []
  [:input
   {:type "button"
    :value "Print filter params"
    :on-click #(re-frame/dispatch [::events/print])}])

(defn Row [{:keys [row columns]}]
  (into [:tr] (map
               (fn [column]
                 (if (:nested? column)
                   (into [:td] (map 
                                (fn [element]
                                  (if (:Cell element)
                                    [(:Cell element) {:row row :value ((:accessor element) row)}]
                                    [:div ((:accessor element) row)])) 
                                (:nested? column)))
                   [:td
                    (if (:Cell column) 
                      [(:Cell column) {:row row :value ((:accessor column) row)}]
                      ((:accessor column) row))]))
               columns)))

(defn Footer [{:keys [columns]}]
  [:tfoot
   (into [:tr] (map
                (fn [column]
                  [:td
                   (when (:Footer column)
                     [(:Footer column)])])
                columns))])

(defn Header [{:keys [columns filter-options]}] 
  [:thead
   (into [:tr]
         (map
          (fn [column]
            [:th (:Header column)])
          columns))
   (into [:tr]
         (map
          (fn [column]
            [:th 
             (when (not (:not-sorted? column))
               [sorter/SortButton {:column column}])])
          columns))
   (into [:tr]
         (map
          (fn [column]
            [:th
             (when (:filter-fields column)
               [filter/FilterBox (:filter-fields column) filter-options])])
          columns))])


(defn Table [{:keys [columns filter-options]}]
  (let [data (re-frame/subscribe [::subs/data])
        columns-filtered (filter #(not (:hidden? %)) columns)
        loading? (re-frame/subscribe [::subs/data-loading?])] 
    (fn []
      [:div
       [Print]
       [pagination/Pagination {:data @data}]
       [:table.table.table-striped {:style {:table-layout "fixed" :width "100%"}}
        [Header {:columns columns-filtered :filter-options filter-options}]
        (when (not @loading?)
          [:tbody
           (for [element @data]
             ^{:key (:id element)}
             [Row {:row element :columns columns-filtered}])])
        (when (and (not @loading?) (seq @data))
          [Footer {:columns columns-filtered}])]
       (if @loading?
         [:h3.text-center "Loading..."]
         (when (empty? @data)
           [:h3.text-center "No data"]))])))
