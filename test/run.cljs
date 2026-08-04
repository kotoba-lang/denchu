#!/usr/bin/env nbb
;; nbb test runner — denchu
;;   nbb --classpath src:test test/run.cljs
(require '[clojure.test :as t]
         'denchu.pole-test
         'denchu.pricing-test
         'denchu.order-test
         'denchu.media-test)

(let [{:keys [fail error]} (t/run-tests 'denchu.pole-test
                                        'denchu.pricing-test
                                        'denchu.order-test
                                        'denchu.media-test)]
  (js/process.exit (if (pos? (+ fail error)) 1 0)))
