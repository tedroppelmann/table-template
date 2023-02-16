(ns re-frame-template.components.table
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]
   [reagent.core :as r]
   [re-frame-template.components.filter :as filter]))


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
                  (if (:Footer column)
                    [:td 
                     [(:Footer column)]]
                    [:td ]))
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
            (if (:filter-fields column)
              [:th 
               [filter/FilterBox (:filter-fields column) filter-options]]
              [:th]))
          columns))])


(defn Table [{:keys [columns filter-options]}]
  (let [data (re-frame/subscribe [::subs/data])
        columns-filtered (filter #(not (:hidden? %)) columns)
        loading? (re-frame/subscribe [::subs/data-loading?])] 
    (fn []
      [:div
       [Print]
       [:table.table.table-striped.table-responsive
        ;; [Headers {:columns columns-filtered :filter-options filter-options}] funciona sin las listas
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
