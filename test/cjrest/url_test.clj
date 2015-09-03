(ns cjrest.url-test
  (:require [clojure.test :refer :all]
            [cjrest.url :refer :all]))

(deftest test-build-url
  (testing "testing build url param"
    (is (= "/foobar/id/:id"
           (build-url "/foobar" :id)))
    (is (= "/foobar/id/:id/name/:name"
           (build-url "/foobar" :id :name)))))

(deftest test-replace-path-params
  (testing "testing replace path params"
    (is (= "/foobar/id/2/name/4"
           (replace-path-params
            "/foobar/id/:id/name/:name"
            {:name 4 :id 2})))))
