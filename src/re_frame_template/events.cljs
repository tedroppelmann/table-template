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
       (assoc :beers result 
              :beers-filtered result
              :data-loading? false))))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (assoc db :failure-http-result result)))

(re-frame/reg-event-db
 ::sort-column-by
 (fn [db [_ key order]]
   (if order 
     (-> db
       (assoc :beers-filtered (sort-by key (:beers-filtered db))))
     (-> db
         (assoc :beers-filtered (reverse (sort-by key (:beers-filtered db))))))))

(re-frame/reg-event-fx
 ::create-query
 (fn [{:keys [db]} [_]]
   {:dispatch [::handler-with-http 
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
                "https://api.punkapi.com/v2/beers?" (-> db :query-map :filter-by))]}))


(re-frame/reg-event-fx
 ::filter
 (fn [{:keys [db]} [_ key type input comparator max-input-value]]
   {:db (assoc-in db [:query-map :filter-by key] {:field-name (name key)
                                                  :input-value input
                                                  :type type
                                                  :comparator comparator
                                                  :max-input-value max-input-value})
    :dispatch [::create-query]}))

(re-frame/reg-event-fx
 ::cancel-filter
 (fn [{:keys [db]} [_ key]]
   (when (-> db :query-map :filter-by key)
     {:db (update-in db [:query-map :filter-by] dissoc key)
      :dispatch [::create-query]})))

(re-frame/reg-event-db
 ::sort
 (fn [db [_ key]]
   (assoc-in db [:query-map :sort-by key] {:field-name (name key)
                                           :order "asc"})))

(re-frame/reg-event-db
 ::print
 (fn [db [_]]
   (js/console.log (:query-map db))))