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
 ::query-map
 (fn [db _]
   (:query-map db)))
