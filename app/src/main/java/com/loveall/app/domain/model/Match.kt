package com.loveall.app.domain.model

data class Match(val id: String, val partner: Profile, val lastMessage: String?, val unread: Int)
