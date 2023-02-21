(ns re-frame-template.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::data
 (fn [db]
   (:data db)))

(re-frame/reg-sub
 ::data-loading?
 (fn [db _]
   (:data-loading? db)))

(re-frame/reg-sub
 ::sort-by
 (fn [db _]
   (-> db :query-map :sort-by)))

(re-frame/reg-sub
 ::page-number
 (fn [db _]
   (-> db :query-map :page-number)))

(re-frame/reg-sub
 ::page-size
 (fn [db _]
   (-> db :query-map :page-size)))

(re-frame/reg-sub
 ::query-map
 (fn [db _]
   (-> db :query-map)))

(re-frame/reg-sub
 ::checked-map
 (fn [db _]
   (-> db :checked-map)))

(re-frame/reg-sub
 ::check-all?
 (fn [db _]
   (-> db :check-all?)))
