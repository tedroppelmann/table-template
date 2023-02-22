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
 (fn [{:keys [db]} [_ url]]                    
   {:db   (assoc db :data-loading? true)   
    :http-xhrio {:method          :get
                 :uri             url
                 :timeout         8000                                           
                 :response-format (ajax/json-response-format {:keywords? true})  
                 :on-success      [::success-http-result]
                 :on-failure      [::failure-http-result]}}))

(re-frame/reg-event-db
 ::success-http-result
 (fn [db [_ result]]
   ;; (js/console.log result)
   (-> db 
       (assoc :data result
              :data-loading? false))))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (assoc db :failure-http-result result)))

(re-frame/reg-event-db
 ::sort-column-by
 (fn [db [_ key order]]
   (js/console.log key)
   (if (= order "asc") 
     (assoc db :data (sort-by key (:data db)))
     (assoc db :data (reverse (sort-by key (:data db)))))))

(re-frame/reg-event-fx
 ::create-query
 (fn [{:keys [db]} [_]]
   {:dispatch [::handler-with-http 
               (let [page-number (-> db :query-map :page-number)
                     page-size (-> db :query-map :page-size)
                     initial (str "https://api.punkapi.com/v2/beers?page=" page-number "&per_page=" page-size "&")]
                 (reduce
                  (fn [endpoint v]
                    (let [value (last v)]
                      (if (= "name" (:field-name value))
                        (if (= (-> value :input-value) "")
                          (str endpoint)
                          (str endpoint "beer_name=" (:input-value value) "&"))
                        (if (and (= "ibu" (:field-name value)) (= "between" (:comparator value)))
                          (if (or (= (-> value :input-value) "") (= "" (:max-input-value value)))
                            (str endpoint)
                            (str endpoint "ibu_gt=" (:input-value value) "&" "ibu_lt=" (:max-input-value value) "&"))
                          (str endpoint)))))
                  initial (-> db :query-map :filter-by)))]}))


(re-frame/reg-event-fx
 ::filter
 (fn [{:keys [db]} [_ key type input comparator max-input-value]]
   {:db (assoc-in db [:query-map :filter-by key] {:field-name (name key)
                                                  :input-value input
                                                  :type type
                                                  :comparator comparator
                                                  :max-input-value max-input-value})
    :fx [[:dispatch [::change-page 1]]
         [:dispatch [::create-query]]]}))

(re-frame/reg-event-fx
 ::cancel-filter
 (fn [{:keys [db]} [_ key]]
   (when (-> db :query-map :filter-by key)
     {:db (update-in db [:query-map :filter-by] dissoc key)
      :fx [[:dispatch [::change-page 1]]
           [:dispatch [::create-query]]]})))

(re-frame/reg-event-fx
 ::sort
 (fn [{:keys [db]} [_ key value]]
   {:db (if (= value "delete")
          ;; (update-in db [:query-map :sort-by] dissoc key)
          (assoc-in db [:query-map :sort-by] (:default-sort-order db))
          (assoc-in db [:query-map :sort-by] {key {:field-name (name key)
                                                   :order value}}))
    :fx [[:dispatch [::change-page 1]]
         ;; [:dispatch [::sort-column-by key value]]
         ]}))

(re-frame/reg-event-db
 ::print
 (fn [db [_]]
   (js/console.log (:query-map db))
   (js/console.log (:checked-map db))))

(re-frame/reg-event-fx
 ::change-page
 (fn [{:keys [db]} [_ new_page]]
   {:db (assoc-in db [:query-map :page-number] new_page)
    :fx [[:dispatch [::reset-check]]
         [:dispatch [::create-query]]]}))

(re-frame/reg-event-db
 ::change-checked-map
 (fn [db [_ row bool key]]
   (if bool 
     (assoc-in db [:checked-map key] {:row row})
     (update-in db [:checked-map] dissoc key))))

(re-frame/reg-event-db
 ::check-all
 (fn [db [_ bool]]
   (if bool
     (assoc db 
            :checked-map (into {} (map (fn [row] {(keyword (str (:id row))) {:row row}}) (:data db))) 
            :check-all? true)
     (assoc db 
            :checked-map {}
            :check-all? false))))

(re-frame/reg-event-db
 ::reset-check
 (fn [db [_]]
   (assoc db
          :checked-map {}
          :check-all? false)))
