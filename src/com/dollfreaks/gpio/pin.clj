(ns com.dollfreaks.gpio.pin
  (:require [com.dollfreaks.gpio.sysfs.pin :as sysfs]))

(def STATES {:high "1" :low "0"})
(def DIRECTIONS {:input "in" :output "out" :high "high" :low "low"})
(def INTERRUPTS {:none "none" :rising "rising" :falling "falling" :all "both"})

(defn- map-contains-value [m v]
  (some (fn [p] (if (= (last p) v) (first p))) m))

(defn set-direction! [p dir]
  (sysfs/set-direction! p (get DIRECTIONS dir))
  p)

(defn input! [p] (set-direction! p :input))

(defn output!
  "Set the pin direction as output, optionally setting the default pin value"
  ([p] (set-direction! p :output))
  ([p v] (set-direction! p v)))

(defn direction [p]
  (map-contains-value DIRECTIONS (sysfs/get-direction p)))

(defn input? [p] (= :input (direction p)))
(defn output? [p] (= :output (direction p)))

(defn set-state! [p s] (sysfs/set-state! p (get STATES s)))
(defn high! [p] (set-state! p :high))
(defn low! [p] (set-state! p :low))

(defn state [p]
  (map-contains-value STATES (sysfs/get-state p)))

(defn high? [p] (= :high (state p)))
(defn low? [p] (= :low (state p)))

(defn interrupt-on! [p e]
  (sysfs/interrupt-on! p (get INTERRUPTS e)))

(defn active-on-low! [p] (sysfs/active-low! p true) p)
(defn active-on-high! [p] (sysfs/active-low! p false) p)

