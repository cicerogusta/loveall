package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.model.Profile
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.domain.repository.PcdCommunityRepository
import com.loveall.app.presentation.state.ExploreState
import com.loveall.app.presentation.state.PcdCommunityState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado próprio, sem herança de ViewModels ou dependências Android de interface. */
class PcdCommunityViewModel(
    private val community: PcdCommunityRepository,
    private val connections: ConnectionsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PcdCommunityState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = operation {
        val member = community.isMember()
        // Esvazia a fila antes da consulta: uma saída em outro aparelho revoga a descoberta.
        mutableState.update { it.copy(member = member, explore = ExploreState()) }
        if (member) loadProfiles()
    }

    fun setMembership(enabled: Boolean, consent: Boolean = false) {
        if (enabled && !consent) return
        operation {
            val member = community.setMembership(enabled, consent)
            mutableState.update { it.copy(member = member, explore = ExploreState()) }
            if (member) loadProfiles()
        }
    }

    private suspend fun loadProfiles() {
        val profiles = community.discover()
        mutableState.update { it.copy(explore = it.explore.copy(profiles = profiles)) }
    }

    fun like(profile: Profile) {
        if (state.value.member != true) return
        operation {
            val matchId = community.like(profile.id)
            removeProfile(profile)
            mutableState.update {
                it.copy(
                    explore =
                        it.explore.copy(
                            matchId = matchId,
                            matchedName = if (matchId != null) profile.name else null,
                        )
                )
            }
        }
    }

    fun pass(profile: Profile) {
        if (state.value.member != true) return
        operation {
            connections.pass(profile.id)
            removeProfile(profile)
        }
    }

    private fun removeProfile(profile: Profile) {
        mutableState.update {
            it.copy(
                explore =
                    it.explore.copy(
                        profiles =
                            it.explore.profiles.filterNot { candidate ->
                                candidate.id == profile.id
                            }
                    )
            )
        }
    }

    fun dismissMatch() {
        mutableState.update {
            it.copy(explore = it.explore.copy(matchId = null, matchedName = null))
        }
    }

    private fun operation(work: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                work()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
