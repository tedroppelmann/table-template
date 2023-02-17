(ns re-frame-template.components.pagination
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]))

(defn Pagination [{:keys [data]}] ;; IDK if it's better use data as an input or call the subscription again
  (let [page-number (re-frame/subscribe [::subs/page-number])]
    [:ul.pagination.justify-content-center
     [:li.page-item {:class (when (= @page-number 1) "disabled")}
      [:a.page-link 
       {:on-click (fn [] (re-frame/dispatch [::events/change-page (dec @page-number)]))}
       "Previous"]]
     [:li.page-item
      [:a.page-link {:style {:font-weight "bold"}} @page-number]] 
     [:li.page-item {:class (when (empty? data) "disabled")}
      [:a.page-link 
       {:on-click (fn [] (re-frame/dispatch [::events/change-page (inc @page-number)]))}
       "Next"]]]))