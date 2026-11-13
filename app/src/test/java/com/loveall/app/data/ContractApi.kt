package com.loveall.app.data

import com.loveall.app.data.remote.api.ConnectionsApi
import com.loveall.app.data.remote.api.MessagesApi
import com.loveall.app.data.remote.api.PlansApi
import com.loveall.app.data.remote.api.ProfileApi
import com.loveall.app.data.remote.api.SafetyApi

interface ContractApi : ProfileApi, ConnectionsApi, MessagesApi, SafetyApi, PlansApi
