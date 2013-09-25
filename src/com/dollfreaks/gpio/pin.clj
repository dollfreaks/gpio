(ns com.dollfreaks.gpio.pin)

(let [gpio-dir "/sys/class/gpio"
      export-file (str gpio-dir "/export")
      unexport-file (str gpio-dir "/unexport")
      STATES {:high "1" :low "0"}]
  (defn- pin-file [p] (str gpio-dir "/gpio" p))
  (defn- value-file [p] (str (pin-file p) "/value"))

  (defn export! [p] (spit export-file p))
  (defn unexport! [p] (spit unexport-file p))

  (defn exported? [p] (.exists (clojure.java.io/file (pin-file p))))

  (defn direction [p])

  (defn set-state! [p s] (spit (value-file p) (s STATES)))

  (defn high! [p] (set-state! p :high))
  (defn low! [p] (set-state! p :low))

  (defn state [p] (case (slurp (value-file p))
                    (:high STATES) :high
                    (:low STATES) :low
                    nil))

  (defn high? [p] (= :high (state p)))
  (defn low? [p] (= :low (state p))))
