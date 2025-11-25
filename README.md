# Gyroscope-Controlled Ball Maze Game

This project demonstrates how to build a **tilt-controlled ball maze game** in  
**Jetpack Compose (Material 3)** using the device’s **gyroscope**.

The player tilts their phone to roll a ball through a simple maze made of  
obstacles and walls, all rendered on a **Canvas** for smooth 2D graphics.

---

## Features

- **Gyroscope-Based Movement**
  - Reads rotational velocity from the gyroscope.
  - Integrates sensor values over time to estimate device tilt.
  - Ball moves in real time according to tilt direction and intensity.

- **Canvas-Rendered Game World**
  - Ball, walls, and obstacles drawn using Compose’s `Canvas`.
  - Customizable maze layout.
  - Soft yellow player ball with stroke outline for game visibility.

- **Maze & Collision Detection**
  - Walls represented as rectangles with simple AABB collision logic.
  - Ball bounces when hitting obstacles or boundaries.
  - Prevents the ball from passing through maze walls.

- **Smooth Movement**
  - Uses Compose animation (`animateFloatAsState`) for natural ball motion.
  - Tilting feels responsive but stable.

---

## Notes

- Uses `Sensor.TYPE_GYROSCOPE`, not the accelerometer.
- Movement is based on integrated gyro values (virtual tilt), not gravity.
