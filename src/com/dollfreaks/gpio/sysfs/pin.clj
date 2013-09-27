(ns com.dollfreaks.gpio.sysfs.pin
  (:require [clojure.java.io :refer (file)]
            [clojure.string :refer (trim-newline)]))


(def gpio-dir (file "/sys/class/gpio"))
(def export-file (file gpio-dir "export"))
(def unexport-file (file gpio-dir "unexport"))
(def pin-dir (memoize (fn [p] (file gpio-dir (str "gpio" p)))))
(def pin-file (memoize (fn [p f] (file (pin-dir p) f))))

(defn- pin-file-fn [f]
  (fn [p] (pin-file p f)))

(def value-file (pin-file-fn "value"))
(def direction-file (pin-file-fn "direction"))
(def edge-file (pin-file-fn "edge"))
(def active-low-file (pin-file-fn "active_low"))

(defn export! [p] (spit export-file p))
(defn unexport! [p] (spit unexport-file p))
(defn exported? [p] (.exists (pin-dir p)))

(defn- valid-direction [d]
  (if (contains? #{"in" "out" "high" "low"} d)
    d
    (throw (Exception. (str "`" d "` is not a valid pin direction")))))

(defn set-direction! [p dir]
  (let [f (direction-file p)]
    (if (.exists f)
      (spit f (valid-direction dir))
      (throw (Exception. (str "Cannot change direction for GPIO pin " p))))))

(defn get-direction [p]
  (trim-newline (slurp (direction-file p))))

(defn- valid-state [s]
  (if (contains? #{"high" "low"} s)
    s
    (throw (Exception. (str "`" s "` is not a valid pin state")))))

(defn set-state! [p s]
  (spit (value-file p) (valid-state s)))

(defn get-state [p]
  (trim-newline (slurp (value-file p))))

(defn- valid-interrupt [i]
  (if (contains? #{"none" "high" "low" "edge"} i)
    i
    (throw (Exception. (str "`" i "` is not a valid interrupt trigger")))))


(defn interrupt-on! [p e]
  (let [f (edge-file p)]
    (if (.exists f)
      (spit f (valid-interript e))
      (throw (Exception. (str "Cannot configure interrupts for GPIO pin " p))))))

(defn active-low! [p s]
  (spin (active-low-file p) (if s 1 0)))
