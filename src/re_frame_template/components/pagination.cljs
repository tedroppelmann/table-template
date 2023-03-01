(ns re-frame-template.components.pagination
  (:require
   [re-frame.core :as re-frame]
   [re-frame-template.events :as events]
   [re-frame-template.subs :as subs]))

(defn Pagination [{:keys [table-key]}] ;; IDK if it's better use data as an input or call the subscription again
  (let [data (re-frame/subscribe [::subs/data table-key])
        page-number (re-frame/subscribe [::subs/page-number table-key])
        page-size (re-frame/subscribe [::subs/page-size table-key])]
    (fn []
      [:ul.pagination
       [:li.page-item {:class (when (= @page-number 1) "disabled")}
        [:a.page-link
         {:on-click (fn [] (re-frame/dispatch [::events/change-page {:new-page (dec @page-number) 
                                                                     :table-key table-key}]))}
         [:i {:class "zmdi zmdi-arrow-left"}]]]
       [:li.page-item.disabled
        [:a.page-link @page-number]]
       [:li.page-item {:class (when (< (count @data) @page-size) "disabled")}
        [:a.page-link
         {:on-click (fn [] (re-frame/dispatch [::events/change-page {:new-page(inc @page-number) 
                                                                     :table-key table-key}]))}
         [:i {:class "zmdi zmdi-arrow-right"}]]]])))