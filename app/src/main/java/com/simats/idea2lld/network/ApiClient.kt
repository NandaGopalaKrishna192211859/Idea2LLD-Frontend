package com.simats.idea2lld.network

import okhttp3.OkHttpClient

object ApiClient {

//    Hello - these are my apis
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }
    // ⚠️ CHANGE IP if needed (same PC where Node.js runs)

//    const val BASE_URL = "http://10.129.73.122:8027/api"
    const val BASE_URL = "http://180.235.121.253:8027/api"
    const val REGISTER_URL = "$BASE_URL/auth/register"
    const val LOGIN_URL = "$BASE_URL/auth/login"
    const val FORGOT_PASSWORD_URL = "$BASE_URL/auth/forgot-password"
    const val MODIFY_PROJECT_URL = "$BASE_URL/projects/dmodify" // + /{pid}

    const val RESET_PASSWORD_URL = "$BASE_URL/auth/reset-password"

    const val RENAME_PROJECT_URL = "$BASE_URL/projects/rename" // + /{pid}

    const val CREATE_DRAFT_URL = "$BASE_URL/projects/create"
    const val GET_DRAFTS_URL = "$BASE_URL/projects/drafts"
    const val GET_SAVED_PROJECTS_URL = "$BASE_URL/projects/saved"
    const val GET_PROJECT_BY_ID_URL = "$BASE_URL/projects"          // + /{pid}
    const val GENERATE_LLD_URL = "$BASE_URL/projects/dgenerate-lld" // + /{pid}

    const val SAVE_CATEGORY_URL = "$BASE_URL/projects/save-category"
    const val SAVE_FEATURES_URL = "$BASE_URL/projects/save-features"

    const val SEND_PROJECT_PACKAGE_URL = "$BASE_URL/investors/send-package"


    const val GET_PROJECT_REQUEST_PACKAGE_URL =
        "$BASE_URL/investors/projects/request"

    const val GET_INVESTOR_FEED_URL =
        "$BASE_URL/investors/feed"



    const val GET_CATEGORIES_URL = "$BASE_URL/questions/categories"

    const val GET_CATEGORY_QUESTIONS_URL = "$BASE_URL/questions"

    const val IMAGE_BASE_URL = "http://180.235.121.253:8027"
//    const val IMAGE_BASE_URL = "http://10.129.73.122:8027"

//
    const val GET_INVESTORS_BY_CATEGORY_URL = "$BASE_URL/investors/category" // + /{category}
    const val GET_PROJECT_PACKAGE_URL = "$BASE_URL/projects/package"


}
