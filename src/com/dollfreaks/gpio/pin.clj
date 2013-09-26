(ns com.dollfreaks.gpio.pin
  (:require [clojure.java.io :refer (file)]
            [clojure.string :refer (trim-newline)]))

(let [gpio-dir "/sys/class/gpio"
      export-file (file gpio-dir "export")
      unexport-file (file gpio-dir "unexport")
      STATES {:high "1" :low "0"}
      DIRECTIONS {:input "in" :output "out" :high "high" :low "low"}
      INTERRUPTS {:none "none" :rising "rising" :falling "falling" :all "both"}
      pin-dir (memoize (fn [p] (file gpio-dir (str "gpio" p))))
      pin-file (memoize (fn [p f] (file (pin-dir p) f)))
      pin-file-fn (fn [f] (fn [p] (pin-file p f)))
      value-file (pin-file-fn "value")
      direction-file (pin-file-fn "direction")
      edge-file (pin-file-fn "edge")
      active-low-file (pin-file-fn "active_low")]

  (defn export! [p] (spit export-file p))
  (defn unexport! [p] (spit unexport-file p))
  (defn exported? [p] (.exists (pin-dir p)))

  (defn set-direction! [p dir]
    (let [f (direction-file p)]
      (if (.exists f)
        (spit f (get DIRECTIONS dir))
        (throw (Exception. (str "Cannot change direction for GPIO pin " p))))))

  (defn input! [p] (set-direction! p :input))

  (defn output!
    "Set the pin direction as output, optionally setting the default pin value"
    ([p] (set-direction! p :output))
    ([p v] (set-direction! p v)))

  (defn direction [p]
    (condp = (trim-newline (slurp (direction-file p)))
      (:input DIRECTIONS) :input
      (:output DIRECTIONS) :output
      nil))

  (defn input? [p] (= :input (direction p)))
  (defn output? [p] (= :output (direction p)))

  (defn set-state! [p s] (spit (value-file p) (s STATES)))
  (defn high! [p] (set-state! p :high))
  (defn low! [p] (set-state! p :low))

  (defn state [p] (condp = (trim-newline (slurp (value-file p)))
                    (:high STATES) :high
                    (:low STATES) :low
                    nil))

  (defn high? [p] (= :high (state p)))
  (defn low? [p] (= :low (state p)))

  (defn set-interrupt-on! [p e]
    (let [f (edge-file p)]
      (if (.exists f)
        (spit f (get INTERRUPTS e))
        (throw (Exception. (str "Cannot configure interrupts for GPIO pin " p))))))

  (defn set-active-on! [p v]
    "Should :low or :high be considered the 'rising' value and vice versa"
    (spit (active-low-file p) (case v :low 1 :high 0 0))))
