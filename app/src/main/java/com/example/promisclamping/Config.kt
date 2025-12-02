package com.example.promisclamping

object Config {
    // API base URLs
//    const val KOMPAUN_BASE_URL = "http://192.168.1.5:8093/"
//    const val KOMPAUN_BASE_URL = "http://10.28.94.178:8093/"

    const val KOMPAUN_BASE_URL = "https://gerbang.lokal.my/api/penguatkuasaan/v1/"
//    const val KOMPAUN_BASE_URL = "https://gerbang.bph.gov.my/api/penguatkuasaan/v1/"

    //    const val UPLOAD_BASE_URL = "http://192.168.1.5:8091/"
//    const val UPLOAD_BASE_URL = "http://10.28.94.178:8091/"

    const val UPLOAD_BASE_URL = "https://gerbang.lokal.my/api/upload/v1/"
//    const val UPLOAD_BASE_URL = "https://gerbang.bph.gov.my/api/upload/v1/"

//    const val PENTADBIRAN_LOGIN_BASE_URL = "http://192.168.1.5:8089/"
    const val PENTADBIRAN_LOGIN_BASE_URL = "http://10.28.94.178:8089/"

//    const val PENTADBIRAN_LOGIN_BASE_URL = "https://gerbang.lokal.my/api/pentadbiran/v1/"
//    const val PENTADBIRAN_LOGIN_BASE_URL = "https://gerbang.bph.gov.my/api/pentadbiran/v1/"

    const val PENTADBIRAN_PUBLIC_BASE_URL = "https://gerbang.bph.gov.my/api/pentadbiran/v1/"

    // S3 / MinIO bucket name
    const val BUCKET_NAME = "kompaun"

//    const val SECURITY_TOKEN =
//        "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJwcm9taXMiLCJzdWIiOiJhZG1pbl9wcm9taXMiLCJpYXQiOjE3NjIyODM1ODksImV4cCI6MTc2MjM2OTk4OX0.PrefUqyJUHwTUPecKyHd7EX4ZDHEoDW6RIHixcvsI9E"

    const val SEC_TOKEN_LOGIN = "admin_promis"
    const val SEC_TOKEN_PASSWORD = "Promis@112233"

    const val DEV_MAC_ADD = "00:80:A3:6E:A6:4C"
}