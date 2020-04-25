# zio-graal-example

Created with `sbt new Clover-Group/zio-template.g8`

target/graalvm-native-image/zio-graal-example

The graal image can be built locally or with docker. It is controled by the sbt setting `graalLocalBuild`.

An image can be built with `graalvm-native-image:packageBin`. The binary will be in `target/graalvm-native-image/zio-graal-example`.  To run the docker image, there is a convenience script `scripts/run-graal-docker`.
