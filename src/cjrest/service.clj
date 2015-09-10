(ns cjrest.service
  (:require [compojure.core :refer [GET POST DELETE PUT]]
            [clj-http.client :as client]
            [cjrest.client :as c]
            [ring.util.response :refer [response]]))

(def request-methods
  {:get (fn [url func] (GET url [] func))
   :post (fn [url func] (POST url [] func))
   :delete (fn [url func] (DELETE url [] func))
   :put (fn [url func] (PUT url [] func))})

(defn consume-endpoint
  [endpoint func]
  (let [endpoint-meta (meta endpoint)]
    (((:method endpoint-meta) request-methods)
     (:path endpoint-meta)
     (fn [req]
       (let [params (:params req)
             body (:body req)]
         (response
          (apply func
                 (remove nil?
                         `(~(if (map? body) body)
                           ~@(map #(% params) (:params endpoint-meta)))))))))))
(defn consume-endpoints
  [& endpoints]
  (for [endpoint endpoints]
    (consume-endpoint (first endpoint) (second endpoint))))
