(ns getclojure.views.helpers
  (:use [clojail.core :only (safe-read)]
        [hiccup.util :only (escape-html)]
        [me.raynes.conch :only (let-programs)])
  (:require [clojure.pprint :as pp]))

(defn pygmentize [s]
  (let-programs [colorize "./pygmentize"
                 pwd "pwd"]
    (colorize "-fhtml" "-lclojure" {:in s :dir "resources/pygments"})))

(defn print-with-code-dispatch [code]
  (with-out-str
    (pp/with-pprint-dispatch pp/code-dispatch
      (pp/pprint (safe-read code)))))

(defn format-input [input]
  (if (string? input)
    (pygmentize (print-with-code-dispatch input))
    (pygmentize (str input "\n"))))

(defn format-value [value]
  (pygmentize value))

(defn format-output [output]
  (if-not (= output "\"\"")
    (pygmentize
      (safe-read
        (with-out-str
          (pp/with-pprint-dispatch pp/code-dispatch
            (pp/pprint output)))))))
