 (ns cjrest.client
     (:require [cjrest.url :refer :all]
               [clj-http.client :as client]
               [clojure.string :refer [replace-first
                                       lower-case]]
               [clojure.data.json :as json]))

(defn request
  [url & {:keys [method body]
      :or {method :get}}]
  {:url url
   :method method
   :body body})

(def request-methods
  {:get client/get
   :post client/post
   :delete client/delete
   :update client/update})

(defn dispatch-request
  "executes the result from (request)"
  [request] 
  (let [body (:body request)]
    (json/read-str
     (:body
      (((:method request) request-methods)
       (:url request)
       {:body (if (not (nil? body))
                (json/write-str body))
        :content-type "application/json"})))))

(defn build-endpoint
  [service-url resource method params]
  {:service service-url
   :path (apply build-url `(~(str "/" (name resource)) ~@params))
   :method method
   :params params})

(defn do-request
  [endpoint body param-map]
  (dispatch-request (make-request endpoint body param-map)))

(defn make-request
  [endpoint body param-map]
  (request (replace-path-params
               (str (var-get (:service endpoint))
                    (:path endpoint))
               param-map)
              :method (:method endpoint)
              :body body))

(defmacro def-method
  [method]
  `(defmacro ~method
     [name# params#]
     (let [param-map#
           (into {} (map (fn [param#]
                           {(keyword param#) param#})
                         params#))
           meta# {:params
                 (into [] (map  #(keyword %) params#))
                 :method (-> '~method lower-case keyword)}]
       `(with-meta
          (fn request-anon
            [~'endpoint#]
            (def ~name#
              (with-meta
                (fn method-anon
                  ~@(remove
                    nil?
                    (list
                     `([~@params#]
                      (do-request ~'endpoint#  nil ~param-map#))
                     (if (= :post (:method meta#))
                         `([~'body# ~@params#]
                           (do-request ~'endpoint# ~'body# ~param-map#))))))
                ~meta#)))
          ~meta#))))

(defn resource
  [res & methods]
  {:resource res
   :methods methods})

(defn service
  [service-url & resources]
  (for [resource resources
        method (:methods resource)]
    (let [method-meta (meta method)]
      (method
       (build-endpoint service-url
                       (:resource resource)
                       (:method method-meta)
                       (:params method-meta))))))

(def-method GET)
(def-method POST)
(def-method DELETE)
(def-method UPDATE)










