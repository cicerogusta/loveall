package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import com.loveall.app.R
import com.loveall.app.ui.components.photoModel
import com.loveall.app.ui.theme.AppSpacing
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun BoxScope.ProfilePhotoGallery(photos: List<String>, name: String) {
    val palette = brandColors
    val pager = rememberPagerState { photos.size }
    val galleryDescription = stringResource(R.string.view_photos)
    HorizontalPager(
        pager,
        modifier =
            Modifier.fillMaxSize().semantics {
                contentDescription = galleryDescription
            },
    ) { index ->
        AsyncImage(
            model = photoModel(photos[index]),
            contentDescription = stringResource(R.string.photo_of, name),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
    if (photos.size > 1)
        Row(
            Modifier.align(Alignment.TopCenter).padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
        ) {
            repeat(photos.size) { index ->
                Surface(
                    Modifier.weight(1f).height(AppSpacing.tiny),
                    shape = CircleShape,
                    color =
                        palette.onHero.copy(alpha = if (pager.currentPage == index) 1f else 0.4f),
                ) {}
            }
        }
}
