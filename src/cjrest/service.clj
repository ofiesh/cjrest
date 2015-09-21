(ns cjrest.service
  (:require [compojure.core :refer [make-route]]
            [cjrest.url :refer [build-url]]
            [ring.util.response :refer [response]]
            [clout.core :refer [route-compile]]))


(defn serve-endpoint-fn
  [params func]
  (fn foobar [req]
    (let [body (:body req)
          req-params (:params req)]
      (apply func
             (remove nil?
                     `(~(if (map? body) body)
                       ~@(map #(% req-params) params)))))))


(defmacro compile-route
  [method path params func]
  (let [keyword-params
        (into [] (map #(keyword %) params))]
    `(let [foo# (serve-endpoint-fn
                 ~keyword-params
                 ~func)]
       (make-route
        ~method
        ~(route-compile (apply build-url `(~path ~@keyword-params)))
        foo#))))

(defmacro GET
  [path params func]
  `(compile-route :get ~path ~params ~func))

(defmacro POST
  [path params func]
  `(compile-route :post ~path ~params ~func))

(defmacro PUT
  [path params func]
  `(compile-route :put ~path ~params ~func))

(defmacro DELETE
  [path params func]
  `(compile-route :delete ~path ~params ~func))

(defn serve-endpoint
  [endpoint func]
  (let [endpoint-meta (meta endpoint)]
    (make-route
     (:method endpoint-meta)
     (:path endpoint-meta)
     (serve-endpoint-fn
      (:params endpoint-meta)
      func))))

((compile-route :get "/foo" [id foo] #(println %1 %2))
 {:request-method :get
  :uri "/foo/id/hello/foo/bar"})
(macroexpand '(GET "/" [:id] #(println %)))



