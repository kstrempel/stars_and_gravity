(ns stars-and-gravity.core)

(def width (atom 0))
(def height (atom 0))

(def particels (atom []))

(def canvas (.getElementById js/document "stars")) 

(def ctx (.getContext canvas "2d"))

(defn init-particels [count]
  (swap! particels
         (fn [_] 
           (map #(hash-map :x (* @width (Math/random))
                           :y (* @height (Math/random))
                           :vx (- (* 2 (Math/random)) 1)
                           :vy (- (* 2 (Math/random)) 1))
                (range count)))))

(defn paint-stars [stars]
  (doseq [star stars]
    (set! (.-fillStyle ctx) "rgba(100,100,100,0.4)")
    (.beginPath ctx)
    (.arc ctx (:x star) (:y star) 1 0 (* 2 Math/PI) false)
    (.fill ctx)
    (.closePath ctx)))

(defn distance [star-a star-b]
  (let [dx (- (:x star-a) (:x star-b))
        dy (- (:y star-a) (:y star-b))]
    {:distance (Math/sqrt (+ (* dx dx) (* dy dy)))
     :star-a star-a
     :star-b star-b}))
    
(defn paint-line [a b]
  (.moveTo ctx (:x a) (:y a))
  (.lineTo ctx (:x b) (:y b))
  (.stroke ctx)
  (.closePath ctx))

(defn distances []
  (mapcat (fn [star-a]
            (map (fn [star-b](distance star-a star-b))
                 @particels))
          @particels))

(defn update-lines []
  (let [calc-distances (distances)
        filtered-distances (filter #(> 50 (:distance %)) calc-distances)]
    (doseq [combination filtered-distances]
      (paint-line (:star-a combination) (:star-b combination)))))

(defn update-particles []
  (swap! particels 
         (fn [particels]
             (map #(let [border (fn [value velocity max]
                                  (let [new (+ value velocity)]
                                    (cond
                                     (> new max) 0
                                     (< new 0) max
                                     :else new)))]
                     (assoc %1 
                       :x (border (:x %1) (:vx %1) @width)
                       :y (border (:y %1) (:vy %1) @height)))
                particels))))
                     
(defn paint []
  (update-particles)
  (update-lines)
  (set! (.-fillStyle ctx) "rgba(255,255,255,0.9)")
  (.fillRect ctx 0 0 @width @height)
  (paint-stars @particels)
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
  (init-particels 30)
  (animate particels))
      
