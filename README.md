# Readme
To generate keys, run the following command:
```
./gradlew run --args="generate"
```
Keys will be shown and saved to `public_key.key` and `private_key.key` files respectively.

To encrypt a file, run the following:
```
./gradlew run --args="encrypt -i input_file -o output_file -k public_key_file
```
To decrypt a file:
```
./gradlew run --args="decrypt -i input_file -o output_file -k private_key_file
```