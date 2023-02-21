(ns re-frame-template.db)

(def default-db
  {:data-loading? true
   :data {}
   :query-map {:filter-by {} :sort-by {} :page-number 1 :page-size 10}
   :checked-map {}
   :check-all? false})
