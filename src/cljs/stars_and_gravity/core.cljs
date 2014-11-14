(ns stars-and-gravity.core)

(def width (atom 0))
(def height (atom 0))
(def lines (atom []))
(def canvas (.getElementById js/document "stars")) 
(def ctx (.getContext canvas "2d"))

(defn init-particels [count]
  (map 
   #(hash-map :x (* @width (Math/random))
              :y (* @height (Math/random))
              :vx (- (* 1.5 (Math/random)) 1)
              :vy (- (* 1.5 (Math/random)) 1)
              :id %)
   (range count)))

(defn paint-stars [particels]
  (let [radius (* 2 Math/PI)]
    (doseq [star particels]
      (.beginPath ctx)
      (set! (.-fillStyle ctx) "rgba(0,0,0,0.5)")
      (.arc ctx (:x star) (:y star) 1 0 radius false)
      (.fill ctx)
      (.closePath ctx))))

(defn distance [star-a star-b]
    (let [dx (- (:x star-a) (:x star-b))
          dy (- (:y star-a) (:y star-b))]
      {:distance (Math/sqrt (+ (* dx dx) (* dy dy)))
       :star-a star-a
       :star-b star-b}))
    
(defn paint-lines [particels]
  (doseq [star-a particels]
    (doseq [star-b particels]
      (if (< (:id star-a) (:id star-b))
        (let [combination (distance star-a star-b)
              d (:distance combination)
              a (:star-a combination)
              b (:star-b combination)]
          (if (> 100 d)
            (do
              (.beginPath ctx)
              (set! (.-strokeStyle ctx) (str "rgba(0,0,0," (/ 3 d) ")"))
              (.moveTo ctx (:x a) (:y a))
              (.lineTo ctx (:x b) (:y b))
              (.stroke ctx)
              (.closePath ctx))))))))
  
(defn update-particles [particels]
  (map #(let [border (fn [value velocity max]
                       (let [new (+ value velocity)]
                         (cond
                          (> new max) 0
                          (< new 0) max
                          :else new)))]
          (assoc %1 
            :x (border (:x %1) (:vx %1) @width)
            :y (border (:y %1) (:vy %1) @height)))
       particels))
                     
(defn paint [particels]
  (set! (.-fillStyle ctx) "rgb(255,255,255)")
  (.clearRect ctx 0 0 @width @height)
  (paint-stars particels)
  (paint-lines particels)
  (.requestAnimationFrame 
   js/window 
   (fn [] (animate (doall (update-particles particels))))))

(defn animate [particels]
  (js/window.setTimeout (paint particels) (/ 1000 60)))

(defn resize []
  (swap! width #(.-innerWidth js/window))
  (swap! height #(.-innerHeight js/window))
  (set! (.-width canvas) @width)
  (set! (.-height canvas) @height))
         
(defn main []
  (resize)
  (set! (.-onresize js/window) #(resize))
  (animate (init-particels 150)))
      
