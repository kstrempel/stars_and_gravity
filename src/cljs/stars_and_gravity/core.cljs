(ns stars-and-gravity.core)

(def width (atom 0))
(def height (atom 0))
(def particels (atom []))
(def lines (atom []))
(def canvas (.getElementById js/document "stars")) 
(def ctx (.getContext canvas "2d"))

(defn init-particels [count]
  (swap! particels
         (fn [i] 
           (map #(hash-map :x (* @width (Math/random))
                           :y (* @height (Math/random))
                           :vx (- (* 2 (Math/random)) 1)
                           :vy (- (* 2 (Math/random)) 1)
                           :id i)
                (range count)))))

(defn paint-stars []
  (.beginPath ctx)
  (set! (.-fillStyle ctx) "rgba(0,0,0,0.5)")
  (doseq [star @particels]
    (.beginPath ctx)
    (.arc ctx (:x star) (:y star) 1 0 (* 2 Math/PI) false)
    (.fill ctx)
    (.closePath ctx))
  (.closePath ctx))

(defn distance [star-a star-b]
  (let [dx (- (:x star-a) (:x star-b))
        dy (- (:y star-a) (:y star-b))]
    {:distance (Math/sqrt (+ (* dx dx) (* dy dy)))
     :star-a star-a
     :star-b star-b}))
    
(defn paint-lines []
  (.beginPath ctx)
  (set! (.-strokeStyle ctx) "rgba(0,0,0,0.1)")
  (doseq [combination @lines]
    (let [a (:star-a combination)
          b (:star-b combination)]                  
      (.moveTo ctx (:x a) (:y a))
      (.lineTo ctx (:x b) (:y b))))
  (.stroke ctx)
  (.closePath ctx))

(defn distances []
  (mapcat (fn [star-a]
            (map (fn [star-b](distance star-a star-b))
                 @particels))
          @particels))

(defn update-lines []
  (let [calc-distances (distances)]
    (swap! lines (fn [_] 
                   (doall
                    (filter #(> 50 (:distance %)) calc-distances))))))

(defn update-particles []
  (swap! particels 
         (fn [particels]
           (doall
             (map #(let [border (fn [value velocity max]
                                  (let [new (+ value velocity)]
                                    (cond
                                     (> new max) 0
                                     (< new 0) max
                                     :else new)))]
                     (assoc %1 
                       :x (border (:x %1) (:vx %1) @width)
                       :y (border (:y %1) (:vy %1) @height)))
                particels)))))
                     
(defn paint []
  (update-particles)
  (update-lines)
  (set! (.-fillStyle ctx) "rgb(255,255,255)")
  (.fillRect ctx 0 0 @width @height)
  (paint-stars)
  (paint-lines)
  (.requestAnimationFrame js/window animate))  

(defn animate [particel]
  (js/window.setTimeout paint 100))

(defn resize []
  (swap! width #(.-innerWidth js/window))
  (swap! height #(.-innerHeight js/window))
  (set! (.-width canvas) @width)
  (set! (.-height canvas) @height))
         
(defn main []
  (resize)
  (set! (.-onresize js/window) #(resize))
  (init-particels 50)
  (animate particels))
      
