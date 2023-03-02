(ns re-frame-template.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::data
 (fn [db [_ table-key]]
   (get-in db (vec (flatten [:resources (get-in db [:tables table-key :data-key]) :data])))))

(re-frame/reg-sub
 ::data-loading?
 (fn [db [_ table-key]]
   (get-in db (vec (flatten [:resources (get-in db [:tables table-key :data-key]) :data-loading?])))))

(re-frame/reg-sub
 ::sort-by
 (fn [db [_ table-key]]
   (get-in db [:tables table-key :query-map :sort-by])))

(re-frame/reg-sub
 ::page-number
 (fn [db [_ table-key]]
   (get-in db [:tables table-key :query-map :page-number])))

(re-frame/reg-sub
 ::page-size
 (fn [db [_ table-key]]
   (get-in db [:tables table-key :query-map :page-size])))

(re-frame/reg-sub
 ::query-map
 (fn [db _]
   (-> db :query-map)))

(re-frame/reg-sub
 ::checked-map
 (fn [db [_ table-key]]
   (-> db :checked-map)
   (get-in db [:tables table-key :checked-map])))

(re-frame/reg-sub
 ::check-all?
 (fn [db [_ table-key]]
   (get-in db [:tables table-key :check-all?])))


