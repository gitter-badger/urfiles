# Urfiles
[![Build Status](https://travis-ci.org/el-bombillo/urfiles.svg?branch=master)](https://travis-ci.org/el-bombillo/urfiles)

RESTful media management service. 
In other words: This service manages _'ur files_... ;)

## Run

1. Download & Install [Java](http://www.oracle.com/technetwork/java/javase/downloads/) >= 1.8
2. Download & Install [Gradle](http://gradle.org/)
3. Start service by running 
   ```
   sh $ gradle run
   ```
4. Use API URL `http://localhost:1760/`

## Icons API

Icon files are located at the resource path: `/media/icons/<service>/<name>`
* `service`: The service name owning the files
* `name`: The filename

Allowed file formats are `PNG` and `JPEG`.

### Upload icon
`curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/path/to/test.png" http://.../media/icons/test/test.png`

... and replace previously uploaded icon
`curl -i -X PUT -H "Content-Type: multipart/form-data" -F "file=@/path/to/test.png" http://.../media/icons/test/test.png`

### Download icon
`curl http://.../media/icons/test/test.png`

### Delete icon
`curl -X DELETE http://.../media/icons/test/test.png`
