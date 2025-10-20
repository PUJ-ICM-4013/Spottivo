package com.example.spottivo.common.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseModule {
    val firestore: FirebaseFirestore by lazy { Firebase.firestore }
}
