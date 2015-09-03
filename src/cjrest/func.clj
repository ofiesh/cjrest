(ns cjrest.func)

(defmacro fn-with-args
  [args body]
  `(fn fn-with-args
     [~@args]
     ~body))
