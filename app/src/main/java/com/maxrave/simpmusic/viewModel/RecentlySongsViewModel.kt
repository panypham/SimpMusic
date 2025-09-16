package com.maxrave.simpmusic.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.maxrave.domain.repository.SongRepository
import com.maxrave.simpmusic.pagination.RecentPagingSource
import com.maxrave.simpmusic.viewModel.base.BaseViewModel

class RecentlySongsViewModel(
    application: Application,
    private val songRepository: SongRepository,
) : BaseViewModel(application) {
    val recentlySongs =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
            ),
        ) {
            RecentPagingSource(songRepository)
        }.flow.cachedIn(viewModelScope)
}