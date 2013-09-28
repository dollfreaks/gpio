(ns com.dollfreaks.gpio.sysfs.pin
  "sysfs filesystem operations"
  (:require [clojure.java.io :refer (file)]
            [clojure.string :refer (trim-newline)]))

(def gpio-dir (file "/sys/class/gpio"))
(def export-file (file gpio-dir "export"))
(def unexport-file (file gpio-dir "unexport"))
(defn pin-dir [p] (file gpio-dir (str "gpio" p)))
(defn pin-file [p f] (file (pin-dir p) f))

(defn- pin-file-fn [f]
  (fn [p] (pin-file p f)))

(def value-file (pin-file-fn "value"))
(def direction-file (pin-file-fn "direction"))
(def edge-file (pin-file-fn "edge"))
(def active-low-file (pin-file-fn "active_low"))

(defrecord GPIOPinFiles [dir value direction edge active-low])
(defrecord GPIOPin [p fs])

(defn gpio-pin [p]
  (GPIOPin.
    p
    (GPIOPinFiles.
      (pin-dir p)
      (value-file p)
      (direction-file p)
      (edge-file p)
      (active-low-file p))))

(defn export! [^GPIOPin p] (spit export-file (:pin p)) p)
(defn unexport! [^GPIOPin p] (spit unexport-file (:pin p)) p)
(defn exported? [^GPIOPin p] (.exists (:dir (:pin p))))

(defn- valid-direction [d]
  (if (contains? #{"in" "out" "high" "low"} d)
    d
    (throw (Exception. (str "`" d "` is not a valid pin direction")))))

(defn set-direction! [^GPIOPin p dir]
  (let [f (:direction (:fs p))]
    (if (.exists f)
      (do (spit f (valid-direction dir)) p)
      (throw (Exception. (str "Cannot change direction for GPIO pin " (:pin p)))))))

(defn get-direction [^GPIOPin p]
  (trim-newline (slurp (:direction (:fs p)))))

(defn- valid-state [s]
  (if (contains? #{"high" "low"} s)
    s
    (throw (Exception. (str "`" s "` is not a valid pin state")))))

(defn set-state! [^GPIOPin p s]
  (spit (:value (:fs p)) (valid-state s)) p)

(defn get-state [^GPIOPin p]
  (trim-newline (slurp (:value (:fs p)))))

(defn- valid-interrupt [i]
  (if (contains? #{"none" "high" "low" "edge"} i)
    i
    (throw (Exception. (str "`" i "` is not a valid interrupt trigger")))))


(defn interrupt-on! [^GPIOPin p e]
  (let [f (:edge (:fs p))]
    (if (.exists f)
      (do (spit f (valid-interrupt e)) p)
      (throw (Exception. (str "Cannot configure interrupts for GPIO pin " p))))))

(defn active-low! [^GPIOPin p s]
  (spit (:active-low (:fs p)) (if s 1 0)) p)
