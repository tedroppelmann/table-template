(ns re-frame-template.events
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.db :as db]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::handler-with-http
 (fn [{:keys [db]} [_ url data-key]]
   {:db   (assoc-in db [:resources data-key :data-loading?] true)
    :http-xhrio {:method          :get
                 :uri             url
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-http-result data-key]
                 :on-failure      [::failure-http-result]}}))

(re-frame/reg-event-db
 ::success-http-result
 (fn [db [_ data-key result]]
   (assoc-in db [:resources data-key] {:data result
                                       :data-loading? false})))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (js/console.log result)))

(re-frame/reg-event-db
 ::sort-column-by
 (fn [db [_ key order]]
   (js/console.log key)
   (if (= order "asc") 
     (assoc db :data (sort-by key (:data db)))
     (assoc db :data (reverse (sort-by key (:data db)))))))

(re-frame/reg-event-fx
 ::create-query-my-server
 (fn [{:keys [db]} [_ data-key]]
   {:dispatch [::handler-with-http "http://localhost:8080/" data-key]}))

(re-frame/reg-event-fx
 ::create-beer-data
 (fn [{:keys [db]} [_ data-key]]
   {:dispatch [::handler-with-http "https://api.punkapi.com/v2/beers?page=1&per_page=10" data-key]}))

(re-frame/reg-event-fx
 ::create-query
 (fn [{:keys [db]} [_ table-key]]
   {:dispatch [::handler-with-http
               (let [page-number (get-in db [:tables table-key :query-map :page-number])
                     page-size (get-in db [:tables table-key :query-map :page-size])
                     initial-url (str "https://api.punkapi.com/v2/beers?page=" page-number "&per_page=" page-size "&")] 
                 (reduce
                  (fn [url v]
                    (let [value (last v)
                          field-name (:field-name value)
                          comparator (:comparator value)]
                      (case field-name
                        "name" (case comparator
                                 "contains" (str url "beer_name=" (:input-value value) "&")
                                 (str url)) 
                        "ibu" (case comparator
                                "between" (str url "ibu_gt=" (:input-value value) "&" "ibu_lt=" (:max-input-value value) "&")
                                "greater-than" (str url "ibu_gt=" (:input-value value) "&")
                                "lower-than" (str url "ibu_lt=" (:input-value value) "&")
                                (str url))
                        "abv" (case comparator
                                "between" (str url "abv_gt=" (:input-value value) "&" "abv_lt=" (:max-input-value value) "&")
                                "greater-than" (str url "abv_gt=" (:input-value value) "&")
                                "lower-than" (str url "abv_lt=" (:input-value value) "&")
                                (str url))
                        (str url))))
                  initial-url (-> db :tables table-key :query-map :filter-by))) 
                  (get-in db [:tables table-key :data-key])]}))

(re-frame/reg-event-fx
 ::filter
 (fn [{:keys [db]} [_ filter-state table-key]]
   (let [accessor (get-in filter-state [:filter-field-selected :accessor])
         type (get-in filter-state [:filter-field-selected :type])
         comparator (get-in filter-state [:dropdown-selection :key])]
     {:db (assoc-in db [:tables table-key :query-map :filter-by accessor]
                    {:field-name (name accessor)
                     :input-value (:filter-input filter-state)
                     :type type
                     :comparator comparator
                     :max-input-value (:filter-input-max filter-state)})
      :fx [(when (:previous-filter-field-accessor filter-state)
             [:dispatch [::delete-previous-filter (:previous-filter-field-accessor filter-state) table-key]]) 
           [:dispatch [::change-page 1 table-key]]]})))

(re-frame/reg-event-db
 ::delete-previous-filter
 (fn [db [_ accessor table-key]]
    (update-in db [:tables table-key :query-map :filter-by] dissoc accessor)))

(re-frame/reg-event-fx
 ::cancel-filter
 (fn [{:keys [db]} [_ key table-key]]
   (when (get-in db [:tables table-key :query-map :filter-by key])
     {:db (update-in db [:tables table-key :query-map :filter-by] dissoc key)
      :fx [[:dispatch [::change-page 1 table-key]]]})))

(re-frame/reg-event-fx
 ::sort
 (fn [{:keys [db]} [_ key value table-key]]
   {:db (if (= value "delete")
          (if (= (count (get-in db [:tables table-key :query-map :sort-by])) 1)
            (assoc-in db [:tables table-key :query-map :sort-by] (get-in db [:tables table-key :default-sort-order]))
            (update-in db [:tables table-key :query-map :sort-by] dissoc key))
          (assoc-in db [:tables table-key :query-map :sort-by key] {:field-name (name key) 
                                                                    :order value}))
    :fx [(when-not (= (get-in db [:tables table-key :query-map :page-number]) 1) 
           [:dispatch [::change-page 1 table-key]])]}))

(re-frame/reg-event-fx
 ::change-page
 (fn [{:keys [db]} [_ new_page table-key]]
   {:db (assoc-in db [:tables table-key :query-map :page-number] new_page)
    :fx [[:dispatch [::reset-check table-key]]
         [:dispatch [::create-query table-key]]]}))

(re-frame/reg-event-db
 ::change-checked-map
 (fn [db [_ row bool key table-key]]
   (if bool 
     (assoc-in db [:tables table-key :checked-map key] {:row row})
     (update-in db [:tables table-key :checked-map] dissoc key))))

(re-frame/reg-event-db
 ::check-all
 (fn [db [_ bool table-key]]
   (if bool
     (update-in db [:tables table-key] 
                assoc 
                :checked-map (into {} (map (fn [row] {(keyword (str (:id row))) {:row row}}) (get-in db [:resources (get-in db [:tables table-key :data-key]) :data])))
                :check-all? true)
     (update-in db [:tables table-key] 
                assoc 
                :checked-map {} 
                :check-all? false))))

(re-frame/reg-event-db
 ::reset-check
 (fn [db [_ table-key]]
   (update-in db [:tables table-key] 
              assoc 
              :checked-map {}
              :check-all? false)))

(re-frame/reg-event-db
 ::create-new-table
 (fn [db [_ table-key]]
   (assoc-in db [:tables table-key] {:data-key table-key
                                     :query-map {:filter-by {} :sort-by {:id {:field-name "id", :order "asc"}} :page-number 1 :page-size 10}
                                     :checked-map {}
                                     :check-all? false
                                     :default-sort-order {:id {:field-name "id", :order "asc"}}})))

(re-frame/reg-event-db
 ::add-row-subcomponent
 (fn [db [_ row-key row]]
   (assoc-in db [:resources row-key] {:data [row] 
                                      :data-loading? false})))

(re-frame/reg-event-db
 ::delete-row-subcomponent
 (fn [db [_ row-key]]
   (update-in db [:tables] dissoc row-key)))