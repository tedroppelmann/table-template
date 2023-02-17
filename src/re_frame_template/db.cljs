(ns re-frame-template.db)

(def default-db
  {:data-loading? true
   :beers {}
   :query-map {:filter-by {} :sort-by {} :page-number 3}
   :sort {}})
