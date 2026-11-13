package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.usecase.AddProfilePhoto
import com.loveall.app.presentation.state.ProfileState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordena o perfil persistido e o rascunho de edição sem acessar componentes Android. A tela
 * observa state e envia ações; somente este ViewModel modifica o fluxo privado. Upload e salvamento
 * são operações distintas, com flags próprias para impedir escritas simultâneas.
 */
class ProfileViewModel(
    private val profiles: ProfileRepository,
    private val addProfilePhoto: AddProfilePhoto,
) : ViewModel() {
    // A UI recebe apenas a visão de leitura; alterações passam pelas ações deste ViewModel.
    private val mutableState = MutableStateFlow(ProfileState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Recarrega a conta da API e cria um novo rascunho a partir da resposta. Como substitui o
     * rascunho, não inicia enquanto uma escrita está em andamento.
     */
    fun refresh() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = profiles.current()
                mutableState.update {
                    it.copy(user = user, edit = ProfileEdit.from(user), saved = false)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    /**
     * Prepara e envia a imagem selecionada, acrescentando a referência devolvida ao rascunho. Isso
     * ainda não salva a lista de fotos no perfil: a associação é confirmada por save.
     *
     * @param reference representação textual da URI recebida do seletor Android.
     */
    fun addPhoto(reference: String) {
        if (state.value.loading || state.value.busy || state.value.edit == null) return
        mutableState.update { it.copy(uploadingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                val draft = state.value.edit ?: throw AuthException(AuthError.VALIDATION)
                val photo = addProfilePhoto(reference, draft.photos)
                mutableState.update { current ->
                    current.copy(
                        edit = current.edit?.let { it.copy(photos = it.photos + photo) },
                        saved = false,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(uploadingPhoto = false) }
            }
        }
    }

    /**
     * Envia o rascunho atual e usa a resposta do servidor como versão confirmada da conta. O sinal
     * saved permite que a UI informe a sessão sobre o usuário atualizado.
     */
    fun save() {
        if (state.value.loading || state.value.busy || state.value.edit == null) return
        // Marca antes de lançar a coroutine para que um segundo toque não inicie outra gravação.
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val user =
                    profiles.update(state.value.edit ?: throw AuthException(AuthError.VALIDATION))
                mutableState.update {
                    it.copy(user = user, edit = ProfileEdit.from(user), saved = true)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }

    /**
     * Aplica uma transformação local ao rascunho, sem requisição HTTP. A cópia evita alterar o
     * valor que a interface já estava observando.
     */
    fun edit(transform: (ProfileEdit) -> ProfileEdit) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(edit = it.edit?.let(transform), saved = false) }
    }
}
