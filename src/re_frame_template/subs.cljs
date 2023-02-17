(ns re-frame-template.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::data
 (fn [db]
   (:beers db)))

(re-frame/reg-sub
 ::data-loading?
 (fn [db _]
   (:data-loading? db)))

(re-frame/reg-sub
 ::sort-by
 (fn [db _]
   (-> db :query-map :sort-by)))
